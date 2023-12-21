package receiver;

import java.util.Random;

public class PacketLoss {

    Random random =new Random();
    public int random(){
        int rand = random.nextInt(25);  // 0 <= rand <10
        return ++rand;
    }
}
