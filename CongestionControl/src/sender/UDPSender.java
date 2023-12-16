package sender;

import same.DataPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.concurrent.Semaphore;

public class UDPSender {
    private static int port = 3000;

    public static void main(String[] args) {

        Semaphore mutex = Mutex.getInstance();

        try {
            int seq = 0;
            PacketLoss packetLoss = new PacketLoss();
            while (true) {

                // Sender to Client Message 입력
                byte[] bytes = setMessage();

                // Message 를 DataPacket 객체에 담고 직렬화 시작
                PacketStream stream = getStream(seq, bytes);

                // Sender to Receiver 소켓,패킷 생성
                DatagramSocket datagramSocket = new DatagramSocket();
                DatagramPacket datagramPacket = new DatagramPacket(stream.buffer(), stream.buffer().length, InetAddress.getByName("localhost"), port);

                // packetLoss
                if(!packetLoss.random()){
                    System.out.println("패킷 잃어버림");
                }
                else{
                    datagramSocket.send(datagramPacket);
                }
                //보냈거나, 패킷 손실해도 seq번호 증가
                seq += bytes.length;

                stream.objectOutputStream().close();

                byte ack[] = new byte[100];
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                //타임아웃 스래드 생성
                Thread thread = new Thread(new LongRunningTask(ackPacket,datagramSocket));
                thread.start();
                Timer timer = new Timer();
                TimeOutTask timeOutTask = new TimeOutTask(thread, timer,datagramPacket,datagramSocket, ackPacket);
                timer.schedule(timeOutTask,3000);


            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static PacketStream getStream(int seq, byte[] bytes) throws IOException {
        System.arraycopy(bytes, 0, bytes, 0, bytes.length);
        DataPacket dataPacket = new DataPacket(seq, bytes.length, bytes);
        System.out.println("dataPacket.toString() = " + dataPacket.toString());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));

        objectOutputStream.flush();
        objectOutputStream.writeObject((Object)dataPacket);
        objectOutputStream.flush();
        byte buffer[] = outputStream.toByteArray();
        PacketStream stream = new PacketStream(objectOutputStream, buffer);
        return stream;
    }

    private record PacketStream(ObjectOutputStream objectOutputStream, byte[] buffer) {
    }

    private static byte[] setMessage() throws IOException {
        System.out.print("\nmessage :");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String readLine = bufferedReader.readLine();
        byte[] bytes = readLine.getBytes();
        return bytes;
    }

    /* TimeOut 발생 */
    public static void TimeOut(DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket) {
        Semaphore mutex = Mutex.getInstance();
        try {
            System.out.println("TimeOut");
            datagramSocket.send(datagramPacket);
            System.out.println("재전송 했음");
            mutex.acquire();
            datagramSocket.receive(ackPacket);
            System.out.println("ackPacket = " + ackPacket.getLength());
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
