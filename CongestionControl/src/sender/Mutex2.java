package sender;

import java.util.concurrent.Semaphore;

public class Mutex2 {
    private static final int PERMITS = 0; // 세마포어 허용 횟수
    private static Semaphore mutex2;

    private Mutex2() {
        // private 생성자로 외부에서 생성을 막음
    }

    public static Semaphore getInstance() {
        if (mutex2 == null) {
            synchronized (Mutex2.class) {
                if (mutex2 == null) {
                    mutex2 = new Semaphore(PERMITS);
                }
            }
        }
        return mutex2;
    }
}
