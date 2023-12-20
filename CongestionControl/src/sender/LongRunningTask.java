package sender;

import same.Congestion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Semaphore;

public class LongRunningTask implements Runnable {

    DatagramPacket ackPacket;
    DatagramSocket datagramSocket;

    Semaphore mutex = Mutex.getInstance();

    // 이 변수로 혼잡제어 하면 됌
    Congestion con = Congestion.getInstance();

    public LongRunningTask(DatagramPacket ackPacket, DatagramSocket datagramSocket) {
        this.ackPacket = ackPacket;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(ackPacket);//block
            int length = ackPacket.getLength();
            System.out.println("ackPacket = " + ackPacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}