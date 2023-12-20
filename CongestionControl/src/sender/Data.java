package sender;

public class Data {
    int pktNum;
    boolean ACKed;  // 패킷이 ACK를 받았는지 여부
    boolean Sent;       // 패킷을 전송했는지 여부
    int NumAcked;     // 패킷이 ACK를 몇번 받았는지
    boolean ReSend; //패킷이 재전송해야하는지
    public Data(int pktNum){
        this.pktNum = pktNum;
        this.ACKed = false;
        this.Sent = false;
        this.NumAcked = 0;
        this.ReSend = false;
    }
    public void setPktNum(int num){
        this.pktNum = num;
    }
    public void setAcked(){
        this.ACKed = true;
    }
    public void setSent() {
        this.Sent = true;
    }
    public void setReSend(){
        this.Sent = true;
    }
    public int getPktNum(){
        return this.pktNum;
    }

    public boolean isACKed() {
        return ACKed;
    }

    public boolean isReSend() {
        return ReSend;
    }

    public boolean isSent() {
        return Sent;
    }
}
