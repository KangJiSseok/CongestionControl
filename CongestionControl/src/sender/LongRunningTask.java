package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LongRunningTask implements Runnable {

    DatagramPacket datagramPacket;
    DatagramSocket datagramSocket;

    public LongRunningTask(DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
        this.datagramPacket = datagramPacket;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        try {
            datagramSocket.receive(datagramPacket);
            System.out.println("server ip : "+datagramPacket.getAddress() + " , server port : "+datagramPacket.getPort());
            System.out.println("ackPacket.getLength() = " + datagramPacket.getLength());
            datagramSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}