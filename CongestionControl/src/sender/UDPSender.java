package sender;


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
    private static int k = 0;
    static List<Thread> timeoutThreads = new ArrayList<>();

    public static void main(String[] args) {

        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();
        Semaphore mutex = Mutex.getInstance();
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
            BufferedReader bufferedReader = new BufferedReader(new FileReader( "C:\\Users\\USER\\Desktop\\CongestionControl\\CongestionControl\\src\\test.txt"));
            // window
            //BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\d\\IdeaProjects\\CongestionControl\\CongestionControl\\src\\test.txt"));

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


            int lastCount = 1;
            boolean ackFinish = false;
            while (true)
            {
                if(ackFinish){
                    break;
                }
                for (i = con.getBase(); i <= con.getBase()+con.getCwnd()-1; i++) {
                    // Sender to Receiver 소켓,패킷 생성
                    int j = con.getNextSeqNum();
                    System.out.println("j = " + j);
                    DatagramSocket datagramSocket = new DatagramSocket();
                    DatagramPacket datagramPacket = new DatagramPacket(
                            stringDataPacketHashMap.get("Packet" + (j - 1)).buffer(),
                            stringDataPacketHashMap.get("Packet" + (j - 1)).buffer().length,
                            InetAddress.getByName("localhost"),
                            port);

                    if(packetLoss.random()){
                        //보내고 나면 nextSeqNum ++;
                        datagramSocket.send(datagramPacket);
                        con.setNextSeqNum();
                        System.out.println("-------------------->" + (j) + "번 패킷 전송");
                    }
                    else{
                        System.out.println("-------------------->" + (j-1) + "번 패킷 사라짐");
                    }

                    stringDataPacketHashMap.get("Packet" + (j - 1)).objectOutputStream().close();

                    byte ack[] = new byte[10000];
                    DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                    //타임아웃 스래드 생성
                    Thread thread = new Thread(new LongRunningTask(ackPacket, datagramSocket, j));
                    thread.start();
                    timeoutThreads.add(thread);
                    Timer timer = new Timer();
                    TimeOutTask timeOutTask = new TimeOutTask(thread, timer, datagramPacket, datagramSocket, ackPacket,i);
                    timer.schedule(timeOutTask, 3000);
                    if(con.getNextSeqNum() == packetLen){
                        ackFinish = true;
                        break;
                    }
                }

                mutex.acquire();
                con.setBase(con.getBase()+con.getCwnd());
                con.setCwnd(con.getCwnd()*2);
                lastCount += con.getCwnd();
                mutex.release();
                Thread.sleep(3000);
                /*
                혼잡제어 시작
                 */
                if(con.isTimeOut()){
                    con.setCwnd(1);

                }

                System.out.println("------------------------------------------------------------------------------------------------------");
            }


            while(true)
            {

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
    public static void TimeOut(DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket,int packetNum) {
        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        Semaphore mutex = Mutex.getInstance();
        try {
            // 타임아웃
            System.out.println("TimeOut");
            datagramSocket.send(datagramPacket);
            System.out.println("-------------------->" + packetNum + "번 패킷 재전송");
            mutex.acquire();
            con.setThreshold(con.getCwnd()/2);
            con.setNextSeqNum();
            con.setTimeOut(true);

            datagramSocket.receive(ackPacket);
            System.out.println("ackPacket = " + ackPacket.getLength());
            mutex.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}