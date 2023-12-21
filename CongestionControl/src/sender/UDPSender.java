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
            //BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "test.txt"));
            // window
            BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\d\\IdeaProjects\\CongestionControl\\CongestionControl\\src\\test.txt"));

            while (true) {
                readLine = bufferedReader.readLine();
                if (readLine == null) break;
                bytes = readLine.getBytes();
                StringHashMap.put("fileLine" + i, bytes);
                i++;
            }

            // 최종 패킷 개수
            int packetLen = i;
            bufferedReader.close();

            StringHashMap.forEach((key, value) ->
            {
                try {
                    stream = getStream(seq, value, k);
                    stringDataPacketHashMap.put("Packet" + k, stream);
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
                if(con.getLastAckNum()==(con.getLastPacketNum()-1)){

                    if(!con.getDuplicated()){ // 한번 중복ACK발생하면 실행되고 이후 중복ACK발생해도 실행x,TimeOut발생 -> 조건문 만족, getDup은 중복ACK가 건드림. 그외엔 항상 조건문 만족
                        if(con.getCwnd()>=con.getThreshold()&&!con.getTurnThreshold()){  //  getTurnThreshold = false
                        con.setCwnd(con.getThreshold());
                        con.setTurnthreshold(true);  // 90행 조건문이 더이상 실행x ->     실행되면 계속해서 cwnd = threshold 가 되어버림.
                            System.out.println("<<Slow-Start>>");}
                    }              // false -> true

                    con.setSendCnt(con.getCwnd());
                    con.setDuplicated(false);
                    System.out.println("\n[ Cwnd : "+con.getCwnd() + " ], [ threshold : "+con.getThreshold()+" ]\n");
                    for (i = con.getBase(); i <= con.getBase()+con.getCwnd()-1; i++) {

                        // Sender to Receiver 소켓,패킷 생성
                        DatagramSocket datagramSocket = new DatagramSocket();
                        DatagramPacket datagramPacket = new DatagramPacket(
                                stringDataPacketHashMap.get("Packet" + con.getLastPacketNum() ).buffer(),
                                stringDataPacketHashMap.get("Packet" + con.getLastPacketNum() ).buffer().length,
                                InetAddress.getByName("localhost"),
                                port);

                        datagramSocket.send(datagramPacket);
                        System.out.println("--------------------> " + con.getLastPacketNum() + "번 패킷 전송");

                        stringDataPacketHashMap.get("Packet" + con.getLastPacketNum() ).objectOutputStream().close();

                        byte ack[] = new byte[10000];
                        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                        //타임아웃 스래드 생성
                        Thread thread = new Thread(new LongRunningTask(ackPacket, datagramSocket, con.getLastPacketNum()));
                        thread.start();

                        Timer timer = new Timer();
                        TimeOutTask timeOutTask = new TimeOutTask(thread, timer, datagramPacket, datagramSocket, ackPacket, con.getLastPacketNum());
                        timer.schedule(timeOutTask, 3000);
                        if(con.getLastPacketNum() == packetLen){
                            ackFinish = true;
                            break;
                        }
                        mutex.acquire();
                        con.plusLastPacketNum();
                        mutex.release();
                    }
                }
                Thread.sleep(3000);
                System.out.println("\n=======================================================");
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
        if((con.getLastAckNum()+2<=i)||i==1) return;

        try {
            System.out.println("\n*************** " + i + "번 패킷 TimeOut! ***************");

            mutex.acquire();

            if(con.getThreshold()>1){
                con.setThreshold(con.getCwnd()/2);
            }
            con.setCwnd(1);
            con.setAckDup(0);
            con.setLastPacketNum(i);
            con.setBase(i+1);
            con.setTurnthreshold(false); // false로 하는 이유 : cwnd가 1로 되어 증가하다 Threshold에 도달하면 선형증가를 하기 위함. 즉 90행 조건문 만족시키기 위함.
            con.InitRecvCnt();
            con.InitSendCnt();

            mutex.release();

            System.out.println("*           <<< Tahoe 알고리즘 작동 >>>          *");
            System.out.println("*               cwnd 1로 변경 -> " + con.getCwnd() + "              *");
            System.out.println("*             임게치 1/2로 설정 -> " + con.getThreshold() + "            *");
            System.out.println("***********************************************\n");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void AckDUP(DatagramSocket datagramSocket, DatagramPacket ackPacket, int ack) {
        // 이 변수로 혼잡제어 하면 됌
        Congestion con = Congestion.getInstance();

        Semaphore mutex = Mutex.getInstance();
        try {
            System.out.println("\n*************** " + ack + "번 Ack 3번 중복! ***************");

            mutex.acquire();

            if(con.getThreshold()>1){
                con.setThreshold(con.getCwnd()/2);
                //con.setTurnthreshold(true);  // GoBackN방식으로 3-Dup-ACK가 여러번 발생하면 Cwnd와 Threshold가 두번 감소하게됨. 따라서 90행의 조건문을 한번만 돌게하기 위함.
                // 추가로 threshold < cwnd 이므로 90행이 실행되어버림 -> 계속해서 cwnd값이 threshold값으로 setting되므로 이를 방지하기 위함.
                // TODO .. 이후 중복ACK사건이 한번 더 발생하게 된다면??
            }
            con.setCwnd(con.getCwnd()/2+3);

            System.out.println("*           <<< Reno 알고리즘 작동 >>>           *");
            System.out.println("*             cwnd 1/2+3로 변경 -> " + con.getCwnd() + "             *");
            System.out.println("*              임게치 1/2로 설정 -> " + con.getThreshold() + "            *");
            System.out.println("************************************************\n");
            con.setAckDup(0);
            con.setLastPacketNum(ack+1);
            con.InitRecvCnt();
            con.InitSendCnt();


            mutex.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}