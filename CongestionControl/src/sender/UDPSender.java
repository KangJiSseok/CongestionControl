package sender;

import receiver.PacketLoss;
import same.Congestion;
import same.DataPacket;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class UDPSender {
    private static int port = 3000;
    static PacketStream stream;
    private static int seq = 0;
    private static int k = 1;
    static LinkedHashMap<Integer, Thread> timeoutThreads = new LinkedHashMap<>();

    private static boolean reSend = false;
    static LinkedHashMap<String, byte[]> StringHashMap = new LinkedHashMap<>();
    static LinkedHashMap<String, PacketStream> stringDataPacketHashMap = new LinkedHashMap<>();

    public static void main(String[] args) {

        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();
        Semaphore mutex = Mutex.getInstance();

        try {

            //파일 읽고 패킷에 미리 담아놓기

            String readLine;
            byte[] bytes;
            int i = 0;
            String path = System.getProperty("user.dir") + "/src/";

            //mac
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "test.txt"));
            // window
//            BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\d\\IdeaProjects\\CongestionControl\\CongestionControl\\src\\test.txt"));

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

                for (i = con.getBase(); i <= con.getBase() + con.getCwnd() - 1; i++)
                {
                    // Sender to Receiver 소켓,패킷 생성

                    DatagramSocket datagramSocket = new DatagramSocket();
//                        DatagramPacket datagramPacket = new DatagramPacket(
//                                stringDataPacketHashMap.get("Packet" + con.getLastPacketNum() ).buffer(),
//                                stringDataPacketHashMap.get("Packet" + con.getLastPacketNum() ).buffer().length,
//                                InetAddress.getByName("localhost"),
//                                port);
                    DatagramPacket datagramPacket = new DatagramPacket(
                            stringDataPacketHashMap.get("Packet" + con.getLastPacketNum()).buffer(),
                            stringDataPacketHashMap.get("Packet" + con.getLastPacketNum()).buffer().length,
                            InetAddress.getByName("localhost"),
                            port);

                    datagramSocket.send(datagramPacket);

                    System.out.println("-------------------->" + con.getLastPacketNum() + "번 패킷 전송");

                    byte ack[] = new byte[10000];
                    DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                    //타임아웃 스래드 생성
                    Thread thread = new Thread(new LongRunningTask(ackPacket, datagramSocket, i));
                    thread.start();
                    timeoutThreads.put(i, thread);
                    Timer timer = new Timer();
                    TimeOutTask timeOutTask = new TimeOutTask(thread, timer, datagramPacket, datagramSocket, ackPacket, con.getLastPacketNum());
                    timer.schedule(timeOutTask, 3000);

                    mutex.acquire();
                    con.plusLastPacketNum();
                    con.setAckDup(0);
                    mutex.release();

                    stringDataPacketHashMap.get("Packet" + i).objectOutputStream().close();

                    if (con.getLastPacketNum() == 22) {
                        ackFinish = true;
                    }
                }


                mutex.acquire();
                con.setUdpNumber(con.getLastPacketNum() - 1);
                mutex.release();

                Thread.sleep(4000);
                mutex.acquire();
                con.setBase(con.getBase() + con.getCwnd());
                System.out.println("main-con.getUdpNumber() = " + con.getUdpNumber());
                con.setCwnd(con.getCwnd() * 2);
                mutex.release();
                System.out.println("-----------------------------------------------------------------------------------------------------------");
                /*
                혼잡제어 시작
                 */

                timeoutThreads.forEach((key, value) -> {
                    try {
                        value.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        value.interrupt();
                    }
                });

                if (ackFinish) {

                    break;
                }

            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.release();
        }
    }

    private static PacketStream getStream(int seq, byte[] bytes, int packetNum) throws IOException {
        System.arraycopy(bytes, 0, bytes, 0, bytes.length);
        DataPacket dataPacket = new DataPacket(seq, bytes.length, bytes, packetNum);
        //System.out.println("dataPacket.toString() = " + dataPacket.toString());
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
            mutex.acquire();
            System.out.println("*** " + i + "번 패킷 TimeOut! ***");
            con.setThreshold(con.getCwnd() / 2);
            con.setCwnd(1);
            System.out.println("cwnd 1로 변경 -> " + con.getCwnd());
            System.out.println("임게치 1/2로 설정 = " + con.getThreshold());
            datagramSocket.send(datagramPacket);
            System.out.println("------------------>" + i + "번 패킷 재전송");

            con.setAckDup(0);

            datagramSocket.receive(ackPacket);
            mutex.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void AckDUP(DatagramSocket datagramSocket, DatagramPacket ackPacket, int ack) {
        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        Semaphore mutex = Mutex.getInstance();
        try {
            mutex.acquire();
            System.out.println("*** " + ack + "번 Ack " + con.getAckDup() + "번 중복! ***");
            con.setThreshold(con.getCwnd() / 2);
            con.setCwnd(con.getThreshold());
            System.out.println("cwnd 1/2로 변경 -> " + con.getCwnd());
            System.out.println("임게치 1/2로 설정 = " + con.getThreshold());
            System.out.println("con.getUdpNumber() = " + con.getUdpNumber());
            System.out.println("con.getLastAckNum() = " + con.getLastAckNum());
            System.out.println("con.getCwnd() = " + con.getCwnd());
            for (int i = ack; i <= (con.getUdpNumber() - 1); i++) {
//                DatagramPacket datagramPacket = new DatagramPacket(
//                        stringDataPacketHashMap.get("Packet" + (ack+1) ).buffer(),
//                        stringDataPacketHashMap.get("Packet" + (ack+1) ).buffer().length,
//                        InetAddress.getByName("localhost"),
//                        port);
                DatagramPacket datagramPacket = new DatagramPacket(
                        stringDataPacketHashMap.get("Packet" + (i + 1)).buffer(),
                        stringDataPacketHashMap.get("Packet" + (i + 1)).buffer().length,
                        InetAddress.getByName("localhost"),
                        port);

                datagramSocket.send(datagramPacket);
                System.out.println("------------------>" + (i + 1) + "번 패킷 재전송");
//                reSend = true;


                con.setAckDup(0);
                con.setLastPacketNum(i + 2);

                datagramSocket.receive(ackPacket);
                System.out.println("<------------------" + (i + 1) + "번 패킷 재전송한거 ack");
                con.setLastAckNum(i + 1);
                System.out.println("datagramPacket = " + con.getLastAckNum());
            }
            mutex.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    public static void cwndUP() {
//        if(cwnd<threshold){ cwnd *= 2; }
//        else { cwnd++; }
//        System.out.println("cwnd : " + cwnd);
//    }
}