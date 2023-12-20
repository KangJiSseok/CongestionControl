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
            System.out.println("<-----" + num + "번 ack 수신");
            //UDPSender.cwndUP();
            if(con.getLastAckNum()==num){
                mutex.acquire();
                con.plusAckDup();
                mutex.release();
            }else if(con.getLastAckNum()+1==num){
                mutex.acquire();
                con.setLastAckNum(num);
                con.setAckDup(1);
                mutex.release();
            }
            if(con.getAckDup()==3){
                UDPSender.AckDUP(datagramSocket, ackPacket, num);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}