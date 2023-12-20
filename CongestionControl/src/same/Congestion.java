package same;

public class Congestion {

    private static Congestion instance;

    //윈도우 크기
    private int cwnd;
    //가장 오래된 패킷의 순서 번호
    private int base;
    //사용 가능하지만 아직 전송 안된 패킷
    private int nextSeqNum;
    //
    private int dupAckCnt;

    private int lastAckNum;
    private int threshold;
    private int lastSentNum;
    private int SeqNum;
    private int lastbyteSent;

    private boolean timeOut;

    public boolean isTimeOut() {
        return timeOut;
    }

    public void setTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }

    private Congestion() {
        cwnd =1;
        base =1;
        nextSeqNum =1;
    }

    public static Congestion getInstance() {
        if (instance == null) {
            instance = new Congestion();
        }
        return instance;
    }


    public int getCwnd() {
        return cwnd;
    }

    public void setCwnd(int cwnd) {
        this.cwnd = cwnd;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getNextSeqNum() {
        return nextSeqNum;
    }

    public void setNextSeqNum() {
        this.nextSeqNum++;
    }

    public int getDupAckCnt() {
        return dupAckCnt;
    }

    public void setDupAckCnt(int dupAckCnt) {
        this.dupAckCnt = dupAckCnt;
    }

    public int getLastAckNum() {
        return lastAckNum;
    }

    public void setLastAckNum(int lastAckNum) {
        this.lastAckNum = lastAckNum;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getLastSentNum() {
        return lastSentNum;
    }

    public void setLastSentNum(int lastSentNum) {
        this.lastSentNum = lastSentNum;
    }

    public int getSeqNum() {
        return SeqNum;
    }

    public void setSeqNum(int seqNum) {
        SeqNum = seqNum;
    }

    public int getLastbyteSent() {
        return lastbyteSent;
    }

    public void setLastbyteSent(int lastbyteSent) {
        this.lastbyteSent = lastbyteSent;
    }
}
