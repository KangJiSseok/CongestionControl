import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    public UDPServer(int port){

        try {
            DatagramSocket datagramSocket = new DatagramSocket(port);
            while (true){
                byte[] buffer = new byte[512];

                //패킷 생성 buffer, buffer.len 크기
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                //클라이언트에서 부터 패킷 수신 (블락)
                System.out.println("client to server data receive wait...");
                datagramSocket.receive(datagramPacket);

                //datagramPacket.getData() 함수는 byte[]로 반환.

                String str = new String(datagramPacket.getData()).trim();

                System.out.println("수신된 데이터 = " + str);

                //IP주소 얻기
                InetAddress address = datagramPacket.getAddress();
                //Port 주소 얻기
                port = datagramPacket.getPort();

                System.out.println("address = " + address);
                System.out.println("port = " + port);

                boolean a = new PacketLoss().random();

                if(a == true){
                    datagramPacket = new DatagramPacket(datagramPacket.getData(), datagramPacket.getData().length, address, port);
                    datagramSocket.send(datagramPacket);
                }
                else{
                    System.out.println("패킷 손실 ");
                }



            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UDPServer(3000);
    }
}
