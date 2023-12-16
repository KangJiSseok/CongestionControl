package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Semaphore;

public class LongRunningTask implements Runnable {

    DatagramPacket datagramPacket;
    DatagramSocket datagramSocket;

    Semaphore mutex = Mutex.getInstance();

    public LongRunningTask(DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
        this.datagramPacket = datagramPacket;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(datagramPacket);
            System.out.println("ackPacket = " + datagramPacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}