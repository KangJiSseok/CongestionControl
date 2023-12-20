package same;

import java.io.Serializable;

public class DataPacket implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 UID
    private int seq;
    private int length;
    private byte[] data;
    private int packetNum;


    public DataPacket(int seq, int length, byte[] data, int packetNum) {
        this.seq = seq;
        this.length = length;
        this.data = data;
        this.packetNum = packetNum;
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
                "packetNum=" + packetNum +
                "seq=" + seq +
                ", length=" + length +
                ", data=" + new String(data) +
                '}';
    }

    public int getPacketNum() {
        return packetNum;
    }

    public void setPacketNum(int packetNum) {
        this.packetNum = packetNum;
    }
}
