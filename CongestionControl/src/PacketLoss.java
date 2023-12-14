import java.util.Random;

public class PacketLoss {

    Random random =new Random();
    public boolean random(){
        int rand = random.nextInt(100);  // 0 <= rand <5
        if(rand >1){
            return true;
        }
        return false;
    }
}
