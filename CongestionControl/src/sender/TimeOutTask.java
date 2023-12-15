package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTask extends TimerTask{

    private Thread thread;
    private Timer timer;

    public TimeOutTask(Thread thread, Timer timer) {
        this.thread = thread;
        this.timer = timer;

    }

    @Override
    public void run() {
        if(thread != null && thread.isAlive()){
            timer.cancel();
            thread.interrupt();
            System.out.println("캔슬됌");
        }
        System.out.println("타임 아웃 됐음");
        timer.cancel();
    }
}
