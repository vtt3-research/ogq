package metavideo.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class EncryptionManager {

    //http://blog.naver.com/PostView.nhn?blogId=since890513&logNo=220199555661&redirect=Dlog&widgetTypeCall=true
    public static String encrypt(String planText){
        String returnString="";

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");//MD2, MD5, SHA, SHA-1, SHA-256, SHA-384, SHA-512
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(planText.getBytes());
        byte byteData[] = md.digest();
        // byte를 string으로 바꾸는 작업을 합니다.
        for(int i = 0 ; i < byteData.length ; i++){
            returnString+=Integer.toString((byteData[i]&0xff) + 0x100, 1).substring(1);
        }

        return returnString;
    }

    public static String getRandomString(){
        String returnString="";

        StringBuffer stringBuffer = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 20; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    // a-z
                    stringBuffer.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    stringBuffer.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    stringBuffer.append((rnd.nextInt(10)));
                    break;
            }
        }

        //출처: http://cofs.tistory.com/266 [CofS]
        returnString=stringBuffer.toString();
        return returnString;
    }

}
