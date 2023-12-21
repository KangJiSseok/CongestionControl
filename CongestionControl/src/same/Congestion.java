package same;

public class Congestion {

    private static Congestion instance;

    private int cwnd;
    private int base;
    private int lastAckNum;
    private int threshold;
    private int lastPacketNum;
    private int ackDup;
    private int sendCnt;
    private int recvCnt;
    private boolean turnthreshold;
    private boolean Duplicated;

    private Congestion(){
        lastAckNum=0;
        lastPacketNum=1;
        ackDup=0;
        base=1;
        cwnd=1;
        threshold=6;
        sendCnt = 0;
        recvCnt = 0;
        turnthreshold = false;
        Duplicated = false;
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

    public int getLastPacketNum() {
        return lastPacketNum;
    }

    public void plusLastPacketNum() {
        this.lastPacketNum++;
    }
    public void setLastPacketNum(int lastPacketNum) {
        this.lastPacketNum = lastPacketNum;
    }

    public int getAckDup() {
        return ackDup;
    }

    public void setAckDup(int ackDup) {
        this.ackDup = ackDup;
    }
    public void plusAckDup() {
        this.ackDup++;
    }

    public void setSendCnt(int sendCnt) {
        this.sendCnt = sendCnt;
    }
    public int getSendCnt(){
        return  sendCnt;
    }
    public void UpRecvCnt(){
        this.recvCnt++;
    }
    public int getRecvCnt(){
        return recvCnt;
    }
    public void InitSendCnt(){
        this.sendCnt = 0;
    }
    public void InitRecvCnt(){
        this.recvCnt = 0;
    }
    public void setTurnthreshold(boolean bool){
        this.turnthreshold = bool;
    }
    public boolean getTurnThreshold(){
        return  turnthreshold;
    }
    public boolean getDuplicated(){
        return Duplicated;
    }
    public void setDuplicated(boolean bool){
        this.Duplicated = bool;
    }

}
