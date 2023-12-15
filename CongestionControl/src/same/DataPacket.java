package same;

import java.io.Serializable;

public class DataPacket implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 UID
    private int seq;
    private int length;
    private byte[] data;

    // 생성자, 게터, 세터 등 필요한 코드...


    public DataPacket(int seq, int length, byte[] data) {
        this.seq = seq;
        this.length = length;
        this.data = data;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "seq=" + seq +
                ", length=" + length +
                ", data=" + new String(data) +
                '}';
    }
}
