package sender;

import same.DataPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class UDPSender {
    private static int port = 3000;
    static PacketStream stream;
    private static int seq = 0;
    private static int k = 0;
    static List<Thread> timeoutThreads = new ArrayList<>();

    public static void main(String[] args) {

        try {
            PacketLoss packetLoss = new PacketLoss();


            //파일 읽고 패킷에 미리 담아놓기
            LinkedHashMap<String, byte[]> StringHashMap = new LinkedHashMap<>();
            LinkedHashMap<String, PacketStream> stringDataPacketHashMap = new LinkedHashMap<>();
            String readLine;
            byte[] bytes;
            int i = 0;
            String path = System.getProperty("user.dir") + "/src/";

            BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "test.txt"));

            while (true) {
                readLine = bufferedReader.readLine();
                if (readLine == null) break;
                bytes = readLine.getBytes();
                StringHashMap.put("fileLine" + i, bytes);
                System.out.println("StringHashMap.get(fileLine" + i + ") = " + StringHashMap.get("fileLine" + i));
                i++;
            }
            int bufferLen = i;
            bufferedReader.close();

            StringHashMap.forEach((key, value) -> {
                try {
                    stream = getStream(seq, value);
                    stringDataPacketHashMap.put("Packet" + k, stream);
                    System.out.println("stringDataPacketHashMap1" + " = " + stringDataPacketHashMap.get("Packet" + k));
                    seq += value.length;
                    k++; // 값을 사용한 후에 증가하도록 이동
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            for (i = 0; i < bufferLen; i++) {
                // Sender to Receiver 소켓,패킷 생성
                DatagramSocket datagramSocket = new DatagramSocket();
                DatagramPacket datagramPacket = new DatagramPacket(
                        stringDataPacketHashMap.get("Packet" + i).buffer(),
                        stringDataPacketHashMap.get("Packet" + i).buffer().length,
                        InetAddress.getByName("localhost"),
                        port);

                // packetLoss
                if (!packetLoss.random()) {
                    System.out.println(i + "번패킷 잃어버림");
                } else {
                    System.out.println(i + "번패킷 전송함");
                    datagramSocket.send(datagramPacket);
                }

                stringDataPacketHashMap.get("Packet" + i).objectOutputStream().close();

                byte ack[] = new byte[10000];
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                //타임아웃 스래드 생성
                Thread thread = new Thread(new LongRunningTask(ackPacket, datagramSocket));
                thread.start();
                timeoutThreads.add(thread);
                Timer timer = new Timer();
                TimeOutTask timeOutTask = new TimeOutTask(thread, timer, datagramPacket, datagramSocket, ackPacket);
                timer.schedule(timeOutTask, 3000);
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 모든 타임아웃 스레드가 종료될 때까지 기다림
        for (Thread timeoutThread : timeoutThreads) {
            try {
                timeoutThread.join();
                timeoutThread.interrupt();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("메인스레드 종료");
        System.exit(0);

    }

    private static PacketStream getStream(int seq, byte[] bytes) throws IOException {
        System.arraycopy(bytes, 0, bytes, 0, bytes.length);
        DataPacket dataPacket = new DataPacket(seq, bytes.length, bytes);
        System.out.println("dataPacket.toString() = " + dataPacket.toString());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));

        objectOutputStream.flush();
        objectOutputStream.writeObject((Object) dataPacket);
        objectOutputStream.flush();
        byte buffer[] = outputStream.toByteArray();
        PacketStream stream = new PacketStream(objectOutputStream, buffer);
        return stream;
    }

    private record PacketStream(ObjectOutputStream objectOutputStream, byte[] buffer) {
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
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
