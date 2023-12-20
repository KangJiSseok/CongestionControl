package same;

import java.io.Serializable;

public class AckPacket implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 UID
    private int ackNumber;
    private int length;

}
