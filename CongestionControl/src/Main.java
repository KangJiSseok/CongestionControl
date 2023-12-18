import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Timer;

public class Main {
    public static void main(String[] args) throws IOException {
        HashMap<String, String> StringHashMap = new HashMap<>();
        String readLine;
        int i=1;
        String path = System.getProperty("user.dir") + "/src/";

        BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "test.txt"));
        while (true) {
            readLine = bufferedReader.readLine();
            if (readLine == null) break;
            StringHashMap.put("packet" + i, readLine);
            i++;
//            System.out.println("readLine = " + readLine);

        }

        // HashMap의 모든 항목 출력
        StringHashMap.forEach((key, value) -> {
            System.out.println(String.format("키 : %s, 값 : %s", key, value));
        });


        bufferedReader.close();
    }
}
