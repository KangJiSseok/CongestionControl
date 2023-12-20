package sender;

import receiver.PacketLoss;
import same.Congestion;
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
    private static int k = 1;
    static List<Thread> timeoutThreads = new ArrayList<>();
    private static int cwnd = 1;
    private static int threshold = 4;
    static int lastnum = 1;
    static int lastAck = 0;
    static int ackDup = 0;


    public static void main(String[] args) {

        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        try {
            PacketLoss packetLoss = new PacketLoss();


            //파일 읽고 패킷에 미리 담아놓기
            LinkedHashMap<String, byte[]> StringHashMap = new LinkedHashMap<>();
            LinkedHashMap<String, PacketStream> stringDataPacketHashMap = new LinkedHashMap<>();
            String readLine;
            byte[] bytes;
            int i = 0;
            String path = System.getProperty("user.dir") + "/src/";

            //mac
            //BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "test.txt"));
            // window
            BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\d\\IdeaProjects\\CongestionControl\\CongestionControl\\src\\test.txt"));

            while (true) {
                readLine = bufferedReader.readLine();
                if (readLine == null) break;
                bytes = readLine.getBytes();
                StringHashMap.put("fileLine" + i, bytes);
                //System.out.println("StringHashMap.get(fileLine" + i + ") = " + StringHashMap.get("fileLine" + i));
                i++;
            }

            // 최종 패킷 개수
            int packetLen = i;
            bufferedReader.close();

            var ref = new Object() {
                int j = 0;
            };
            StringHashMap.forEach((key, value) ->
            {
                try {
                    stream = getStream(seq, value, k);
                    stringDataPacketHashMap.put("Packet" + k, stream);
                    //System.out.println("stringDataPacketHashMap" + ref.j++ + " = " + stringDataPacketHashMap.get("Packet" + k));
                    seq += value.length;
                    k++; // 값을 사용한 후에 증가하도록 이동
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            boolean ackFinish = false;
            while (true)
            {
                if(ackFinish){
                    break;
                }
                for (i = 1; i <= cwnd; i++) {
                    // Sender to Receiver 소켓,패킷 생성

                    DatagramSocket datagramSocket = new DatagramSocket();
                    DatagramPacket datagramPacket = new DatagramPacket(
                            stringDataPacketHashMap.get("Packet" + lastnum ).buffer(),
                            stringDataPacketHashMap.get("Packet" + lastnum ).buffer().length,
                            InetAddress.getByName("localhost"),
                            port);

                    datagramSocket.send(datagramPacket);
                    System.out.println("-------------------->" + lastnum + "번 패킷 전송");

                    stringDataPacketHashMap.get("Packet" + lastnum ).objectOutputStream().close();

                    byte ack[] = new byte[10000];
                    DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                    //타임아웃 스래드 생성
                    Thread thread = new Thread(new LongRunningTask(datagramPacket, ackPacket, datagramSocket, lastnum));
                    thread.start();
                    timeoutThreads.add(thread);
                    Timer timer = new Timer();
                    TimeOutTask timeOutTask = new TimeOutTask(thread, timer, datagramPacket, datagramSocket, ackPacket, lastnum);
                    timer.schedule(timeOutTask, 3000);
                    if(lastnum == packetLen){
                        ackFinish = true;
                        break;
                    }
                    lastnum++;
                }
                Thread.sleep(3000);
                /*
                혼잡제어 시작
                 */
            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static PacketStream getStream(int seq, byte[] bytes, int packetNum) throws IOException {
        System.arraycopy(bytes, 0, bytes, 0, bytes.length);
        DataPacket dataPacket = new DataPacket(seq, bytes.length, bytes, packetNum);
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
    public static void TimeOut(DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket, int i) {
        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        Semaphore mutex = Mutex.getInstance();
        try {
            System.out.println("*** " + i + "번 패킷 TimeOut! ***");
            threshold = cwnd/2;
            cwnd = 1;
            System.out.println("cwnd 1로 변경 -> " + cwnd);
            System.out.println("임게치 1/2로 설정 = " + threshold);
            datagramSocket.send(datagramPacket);
            System.out.println( "------------------>" + i + "번 패킷 재전송");
            ackDup=0;

            mutex.acquire();
            datagramSocket.receive(ackPacket);
            mutex.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void AckDUP(DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket, int i) {
        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        Semaphore mutex = Mutex.getInstance();
        try {
            System.out.println("*** " + i + "번 패킷 3번 중복! ***");
            threshold = cwnd/2;
            cwnd = threshold;
            System.out.println("cwnd 1/2로 변경 -> " + cwnd);
            System.out.println("임게치 1/2로 설정 = " + threshold);
            datagramSocket.send(datagramPacket);
            System.out.println( "------------------>" + (i+1) + "번 패킷 재전송");
            ackDup = 0;

            mutex.acquire();
            datagramSocket.receive(ackPacket);
            mutex.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void cwndUP() {
        if(cwnd<threshold){ cwnd *= 2; }
        else { cwnd++; }
        System.out.println("cwnd : " + cwnd);
    }
}