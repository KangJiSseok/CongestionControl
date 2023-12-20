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
    DatagramPacket datagramPacket;

    // 이 변수로 혼잡제어 하면 됌
    Congestion con = Congestion.getInstance();

    public LongRunningTask(DatagramPacket datagramPacket, DatagramPacket ackPacket, DatagramSocket datagramSocket, int packetNum) {
        this.ackPacket = ackPacket;
        this.datagramSocket = datagramSocket;
        this.packetNum = packetNum;
        this.datagramPacket = datagramPacket;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(ackPacket);//block
            ByteBuffer buffer = ByteBuffer.wrap(ackPacket.getData());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("<-----" + buffer.getInt() + "번 ack 수신");
            UDPSender.cwndUP();
            if(UDPSender.lastAck==buffer.getInt()){
                UDPSender.ackDup++;
            }else{
                UDPSender.lastAck=buffer.getInt();
            }
            if(UDPSender.ackDup==3){
                UDPSender.AckDUP(datagramPacket, datagramSocket, ackPacket, buffer.getInt());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}