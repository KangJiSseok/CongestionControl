package sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;

public class UDPSender {
    private static int port = 3000;

    public static void main(String[] args) {

        try {
            PacketLoss packetLoss = new PacketLoss();
            while (true) {
                //패킷 하나만 전달
                DatagramSocket datagramSocket = new DatagramSocket();
                System.out.print("\nmessage : \n");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String readLine = bufferedReader.readLine();

                // packetLoss
                if(!packetLoss.random()){
                    System.out.println("패킷 잃어버림");
                }
                //패킷 잃어버렸을 때 만들어야함 . seq번호로
                byte buffer[] = readLine.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port);
                datagramSocket.send(datagramPacket);

                byte ack[] = new byte[100];
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, InetAddress.getByName("localhost"), port);
                //타임아웃 스래드 생성
                Thread thread = new Thread(new LongRunningTask(ackPacket,datagramSocket));
                thread.start();
                Timer timer = new Timer();
                TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
                timer.schedule(timeOutTask,3000);

            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
