package receiver;


import same.DataPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPReceiver {

    public UDPReceiver(int port) {

        try {
            DatagramSocket datagramSocket = new DatagramSocket(port);
            ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

            while (true) {

                byte[] receivedData = new byte[512];
                DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);

                datagramSocket.receive(datagramPacket);

                // 직렬화된 데이터 추출
                byte[] serializedData = datagramPacket.getData();

                // ByteArrayInputStream을 사용하여 직렬화된 데이터로부터 객체 역직렬화
                ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedData);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // 역직렬화하여 객체로 변환
                DataPacket receivedObject = null;
                try {
                    receivedObject = (DataPacket) objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // 수신된 데이터 사용
                //System.out.println("Received Object: " + receivedObject.toString());
                System.out.println( "------------------>" + receivedObject.getPacketNum() + "번 패킷 수신" );

                accumulatedData.write(serializedData, 0, receivedObject.getLength());
                //역직렬화

                //IP주소 얻기
                InetAddress address = datagramPacket.getAddress();
                //Port 주소 얻기
                port = datagramPacket.getPort();

                //ackPacket 생성
                byte[] ack = accumulatedData.toByteArray();
                //System.out.println("ack.length = " + ack.length);
                DatagramPacket ackSendPacket = new DatagramPacket(ack, ack.length, address, port);
                datagramSocket.send(ackSendPacket);
                System.out.println( "<------" + receivedObject.getPacketNum() + "번 ack 송신");

                // 필요에 따라 스트림을 닫아주는 것이 좋습니다.
                objectInputStream.close();
                inputStream.close();

                //여기까지

                //datagramPacket.getData() 함수는 byte[]로 반환.
//
//                String str = new String(datagramPacket.getData()).trim();
//                System.out.println("수신된 데이터 = " + str);

                // datagramPacket.getData() 함수는 byte[]로 반환.
                //System.out.println("receivedObject.getSeq() = " + receivedObject.getSeq());
                //System.out.println("receivedObject.getLength() = " + receivedObject.getLength());
                //System.out.println("receivedObject.getData() = " + new String(receivedObject.getData()));



            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UDPReceiver(3000);
    }
}
