package sender;

import java.util.concurrent.Semaphore;

public class Mutex {
    private static final int PERMITS = 1; // 세마포어 허용 횟수
    private static Semaphore mutex;

    private Mutex() {
        // private 생성자로 외부에서 생성을 막음
    }

    public static Semaphore getInstance() {
        if (mutex == null) {
            synchronized (Mutex.class) {
                if (mutex == null) {
                    mutex = new Semaphore(PERMITS);
                }
            }
        }
        return mutex;
    }
}
