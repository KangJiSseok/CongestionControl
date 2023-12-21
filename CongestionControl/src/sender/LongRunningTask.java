package sender;

import same.Congestion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

public class LongRunningTask implements Runnable {

    DatagramPacket ackPacket;
    DatagramSocket datagramSocket;
    int packetNum;
    Semaphore mutex = Mutex.getInstance();

    // 이 변수로 혼잡제어 하면 됌
    Congestion con = Congestion.getInstance();

    public LongRunningTask( DatagramPacket ackPacket, DatagramSocket datagramSocket, int packetNum) {
        this.ackPacket = ackPacket;
        this.datagramSocket = datagramSocket;
        this.packetNum = packetNum;
    }

    @Override
    public void run() {

        Semaphore mutex = Mutex.getInstance();

        int num;
        try {
            datagramSocket.receive(ackPacket);//block
            ByteBuffer buffer = ByteBuffer.wrap(ackPacket.getData());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            num = buffer.getInt();
            con.UpRecvCnt(); // 수정 필요
            System.out.println("<-----" + num + "번 ack 수신");
            if(con.getLastAckNum()==num){
                mutex.acquire();
                con.plusAckDup();
                mutex.release();
            }else if(con.getLastAckNum()+1==num){
                mutex.acquire();
                con.setLastAckNum(num);
                con.setAckDup(1);
                // cwnd증가
                // 쓰레쉬 홀드는 Sender에서.
                    if(con.getSendCnt()==con.getRecvCnt()){
                        // System.out.println("cwnd : "+con.getCwnd());
                        con.InitSendCnt();
                        con.InitRecvCnt();

                        con.setBase(con.getBase()+con.getCwnd());


                        if(con.getCwnd()<con.getThreshold()){ con.setCwnd(con.getCwnd()*2); }
                        else {
                            //System.out.println("<<<Slow-Start>>>");
                            con.setCwnd(con.getCwnd()+1);}
                        //System.out.println("실행됨");
                        //System.out.println("con.getRecvCnt() = " + con.getRecvCnt());
                        //System.out.println("con.getSendCnt() = " + con.getSendCnt());

                    }


                mutex.release();
            }
            if(con.getAckDup()==3&&!con.getDuplicated()){   // 중복이 처음 발생했을때 작동. (패킷이많아져서 3dup이 여러번 실행될 수 있기 때문)
                UDPSender.AckDUP(datagramSocket, ackPacket, num);
                con.setDuplicated(true);    // 한번만 실행되게끔 하는 함수.

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}