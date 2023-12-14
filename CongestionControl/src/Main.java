import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        Thread thread = new Thread(main.new LongRunningTask());
        thread.start();

        Timer timer = new Timer();
        TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
        timer.schedule(timeOutTask, 3000);
    }

    class LongRunningTask implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 10; i++) {
                    System.out.println("Processing step " + i);

                    Thread.sleep(1000); // 1초 동안 일시 정지
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            }

            System.out.println("Long running task completed.");
        }
    }
}
