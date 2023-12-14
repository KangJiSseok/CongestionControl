import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class LongRunningTask implements Runnable {

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
            System.out.println("수신된 데이터 : "+ new String(datagramPacket.getData()).trim());
            datagramSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}