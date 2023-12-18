package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Semaphore;

public class LongRunningTask implements Runnable {

    DatagramPacket ackPacket;
    DatagramSocket datagramSocket;

    Semaphore mutex = Mutex.getInstance();

    public LongRunningTask(DatagramPacket ackPacket, DatagramSocket datagramSocket) {
        this.ackPacket = ackPacket;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(ackPacket);  //block
            System.out.println("ackPacket = " + ackPacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}