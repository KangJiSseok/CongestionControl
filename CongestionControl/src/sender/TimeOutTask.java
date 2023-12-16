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
    Semaphore mutex = Mutex.getInstance();

    public TimeOutTask(Thread thread, Timer timer, DatagramPacket datagramPacket, DatagramSocket datagramSocket, DatagramPacket ackPacket) {
        this.thread = thread;
        this.timer = timer;
        this.datagramPacket = datagramPacket;
        this.datagramSocket = datagramSocket;
        this.ackPacket = ackPacket;
    }

    @Override
    public void run() {
        if(thread != null && thread.isAlive()){
            try {
                UDPSender.TimeOut(datagramPacket,datagramSocket,ackPacket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            timer.cancel();
            thread.interrupt();
        }
        timer.cancel();
    }
}
