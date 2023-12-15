package sender;

import java.util.Random;

public class PacketLoss {

    Random random =new Random();
    public boolean random(){
        int rand = random.nextInt(10);  // 0 <= rand <10
        if(rand >1){
            return true;
        }
        return false;
    }
}
