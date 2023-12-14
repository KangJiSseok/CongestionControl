import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;

public class UDPClient {
    private String str;
    private BufferedReader file;
    private static int SERVERPORT=3000;
    public UDPClient(String ip,int port){
        try{

            //패킷 초기화
            InetAddress address = InetAddress.getByName(ip);
            DatagramSocket datagramSocket = new DatagramSocket(port);
            System.out.print("message : ");
            file = new BufferedReader(new InputStreamReader(System.in));
            str = file.readLine();
            byte buffer[] = str.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,address,SERVERPORT);
            datagramSocket.send(datagramPacket);
            buffer = new byte[512];
            // client datapacket 생성
            datagramPacket = new DatagramPacket(buffer,buffer.length);
            // client receive wait
            System.out.println("server to client data receive wait...");

            Thread thread = new Thread(new LongRunningTask(datagramPacket,datagramSocket));
            thread.start();


            Timer timer = new Timer();
            TimeOutTask timeOutTask = new TimeOutTask(thread, timer);

            timer.schedule(timeOutTask, 3000);

//            datagramSocket.receive(datagramPacket);
//            System.out.println("server ip : "+datagramPacket.getAddress() + " , server port : "+datagramPacket.getPort());
//            System.out.println("수신된 데이터 : "+ new String(datagramPacket.getData()).trim());
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args){
        new UDPClient("localhost",2000);

    }
}
