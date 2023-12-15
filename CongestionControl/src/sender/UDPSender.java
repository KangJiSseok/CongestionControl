package sender;

import same.DataPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;

public class UDPSender {
    private static int port = 3000;

    public static void main(String[] args) {

        try {
            int seq = 0;
            PacketLoss packetLoss = new PacketLoss();
            while (true) {
                //패킷 하나만 전달
                DatagramSocket datagramSocket = new DatagramSocket();
                System.out.print("\nmessage : \n");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String readLine = bufferedReader.readLine();
                byte[] bytes = readLine.getBytes();
                //전송할 DataPacket을 stream화 시작
                byte[] byteToDataPacket = new byte[bytes.length];
                System.arraycopy(bytes, 0, byteToDataPacket, 0, bytes.length);
                DataPacket dataPacket = new DataPacket(seq,readLine.length(),byteToDataPacket);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));

                objectOutputStream.flush();
                objectOutputStream.writeObject((Object)dataPacket);
                objectOutputStream.flush();


                // packetLoss
                if(!packetLoss.random()){
                    System.out.println("패킷 잃어버림");
                }
                //패킷 잃어버렸을 때 만들어야함 . seq번호로
                byte buffer[] = outputStream.toByteArray();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port);
                datagramSocket.send(datagramPacket);

                objectOutputStream.close();

                byte ack[] = new byte[100];
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                //타임아웃 스래드 생성
                Thread thread = new Thread(new LongRunningTask(ackPacket,datagramSocket));
                thread.start();
                Timer timer = new Timer();
                TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
                timer.schedule(timeOutTask,3000);

            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
