package sender;

import same.Congestion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
            System.out.println("con.getLastAckNum() = " + con.getLastAckNum());
            System.out.println("num = " + num);
            if (con.getLastAckNum() == num) {
                mutex.acquire();
                con.plusAckDup();
//                System.out.println("aaacon.getAckDup() = " + con.getAckDup());
                mutex.release();
            } else if (con.getLastAckNum() + 1 == num) {
                mutex.acquire();
                con.setLastAckNum(num);
//                System.out.println("con.getLastAckNum() = " + con.getLastAckNum());
                con.setAckDup(1);
//                System.out.println("con.getAckDup() = " + con.getAckDup());
                mutex.release();
            }

            System.out.println("con.getAckDup() = " + con.getAckDup());
            if (con.getAckDup() == 4) {
                UDPSender.AckDUP(datagramSocket, ackPacket, num);
            } else if (con.getAckDup() == 2) {
                System.out.println("con.getLastPacketNum() = " + con.getLastPacketNum());
                if (packetNum == con.getLastPacketNum()-1) {
                    UDPSender.AckDUP(datagramSocket, ackPacket, num);
                }
                if (packetNum == con.getLastPacketNum()+2) {
                    UDPSender.AckDUP(datagramSocket, ackPacket, num);
                }
            } else if (con.getAckDup() == 3) {
                System.out.println("con.getLastPacketNum() = " + con.getLastPacketNum());
                if (packetNum == con.getLastPacketNum()-1) {
                    UDPSender.AckDUP(datagramSocket, ackPacket, num);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}