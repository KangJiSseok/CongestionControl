package sender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class TimeOutTask extends TimerTask{

    private Thread thread;
    private Timer timer;
    DatagramPacket datagramPacket;
    DatagramSocket datagramSocket;

    DatagramPacket ackPacket;
    int packetNum;


    public TimeOutTask(Thread thread, Timer timer, DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket, int packetNum) {
        this.thread = thread;
        this.timer = timer;
        this.datagramPacket = datagramPacket;
        this.datagramSocket = datagramSocket;
        this.ackPacket = ackPacket;
        this.packetNum = packetNum;
    }

    @Override
    public void run() {
        if(thread != null && thread.isAlive()){
            try {
                UDPSender.TimeOut(datagramPacket,datagramSocket,ackPacket, packetNum);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            finally {
                timer.cancel();
                thread.interrupt();
            }
        }
        timer.cancel();
    }


}
