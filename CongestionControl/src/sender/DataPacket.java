package sender;

public class DataPacket {

    int seq;
    int dataLen;
    byte[] data;

    public DataPacket(int seq, int dataLen, byte[] data) {
        this.seq = seq;
        this.dataLen = dataLen;
        this.data = data;
    }
}
