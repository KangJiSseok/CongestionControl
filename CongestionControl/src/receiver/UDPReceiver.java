package receiver;


import same.DataPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class UDPReceiver {

    int lastAck = 0;
    boolean resend = false;
    int rand = 0;

    public UDPReceiver(int port) {

        try {
            DatagramSocket datagramSocket = new DatagramSocket(port);
            ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();
            PacketLoss packetLoss = new PacketLoss();
            rand = packetLoss.random();
            System.out.println("오류 패킷번호 : " + rand);

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


                if((receivedObject.getPacketNum()==rand && !resend)||(lastAck+1<receivedObject.getPacketNum())){
                    if(rand%2==0){
                        //수신오류
                        System.out.println("-------------------->" + receivedObject.getPacketNum() + "번 패킷 수신오류!!");
                        receivedObject.setPacketNum(lastAck); // 정상수신된 마지막 패킷번호로 변경
                        resend=true;
                    } else {
                        //패킷손실
                        System.out.println("*************** " + receivedObject.getPacketNum() + "번 패킷 손실! ***************");
                        resend=true;
                        continue;
                    }
                }
                else {
                    //정상
                    // 수신된 패킷 번호
                    System.out.println("-------------------->" + receivedObject.getPacketNum() + "번 패킷 수신");
                    if((lastAck+1)==receivedObject.getPacketNum()) {
                        lastAck++;
                    }

                    //역직렬화
                    accumulatedData.write(serializedData, 0, receivedObject.getLength());
                }

                //IP주소 얻기
                InetAddress address = datagramPacket.getAddress();
                //Port 주소 얻기
                port = datagramPacket.getPort();

                //ackPacket 생성, ack번호 담기
                ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
                ByteOrder order = ByteOrder.LITTLE_ENDIAN;
                buff.order(order);
                buff.putInt(lastAck);
                byte[] ack = buff.array();

                DatagramPacket ackSendPacket = new DatagramPacket(ack, ack.length, address, port);
                datagramSocket.send(ackSendPacket);

                // 송신한 패킷 번호 (=ack번호)
                System.out.println( "<------" + lastAck + "번 ack 송신");

                // 필요에 따라 스트림을 닫아주는 것이 좋습니다.
                objectInputStream.close();
                inputStream.close();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new UDPReceiver(3000);
    }
}
