package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Semaphore;

public class LongRunningTask implements Runnable {

    DatagramPacket ackPacket;
    DatagramSocket datagramSocket;
    int i;
    Semaphore mutex = Mutex.getInstance();

    public LongRunningTask(DatagramPacket ackPacket, DatagramSocket datagramSocket, int i) {
        this.ackPacket = ackPacket;
        this.datagramSocket = datagramSocket;
        this.i = i;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(ackPacket);  //block
            //System.out.println("ackPacket = " + ackPacket.getLength());
            System.out.println( "<------" + i + "번 ack 수신");
            UDPSender.cwndUP();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}