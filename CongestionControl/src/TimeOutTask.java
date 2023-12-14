import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTask extends TimerTask{

    private Thread thread;
    private Timer timer;
    DatagramPacket datagramPacket;
    DatagramSocket datagramSocket;

    public TimeOutTask(Thread thread, Timer timer) {
        this.thread = thread;
        this.timer = timer;
    }

    public TimeOutTask(Thread thread, Timer timer, DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
        this.thread = thread;
        this.timer = timer;
        this.datagramPacket = datagramPacket;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        if(thread != null && thread.isAlive()){
            thread.interrupt();
            timer.cancel();
            System.out.println("캔슬됌");
        }
        System.out.println("타임 아웃 됐음");
        timer.cancel();
    }
}
