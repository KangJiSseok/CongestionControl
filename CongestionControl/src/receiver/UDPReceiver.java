package receiver;


import sender.PacketLoss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPReceiver {

    public UDPReceiver(int port){

        try {
            DatagramSocket datagramSocket = new DatagramSocket(port);
            ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

            while (true){
                System.out.println("프린트 다시 시작");
                byte[] buffer = new byte[512];

                //패킷 생성 buffer, buffer.len 크기
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                //클라이언트에서 부터 패킷 수신 (블락)
                System.out.println("client to server data receive wait...");
                datagramSocket.receive(datagramPacket);

                //datagramPacket.getData() 함수는 byte[]로 반환.

                String str = new String(datagramPacket.getData()).trim();
                System.out.println("수신된 데이터 = " + str);

                // datagramPacket.getData() 함수는 byte[]로 반환.
                byte[] receivedData = datagramPacket.getData();
                System.out.println("수신된 데이터 길이 = " + datagramPacket.getLength());

                accumulatedData.write(receivedData,0,datagramPacket.getLength());
                //IP주소 얻기
                InetAddress address = datagramPacket.getAddress();
                //Port 주소 얻기
                port = datagramPacket.getPort();
                System.out.println("address = " + address);
                System.out.println("port = " + port);

                //ackPacket 생성
                byte[] ack = accumulatedData.toByteArray();
                DatagramPacket ackSendPacket = new DatagramPacket(ack, ack.length,address,port);
                datagramSocket.send(ackSendPacket);
                System.out.println("ack byte 만큼 보내기 완료= " + ack);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UDPReceiver(3000);
    }
}
