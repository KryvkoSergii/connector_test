package test;


import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by srg on 30.06.16.
 */
public class Test {
    private static String messageInString = "0000005600000003001000100000000f7fffffff00001388001c02960fffffff000003ff00000003000000000000000000000000010c4354494f5353657276657200020c4354494f5353657276657200030c4354494f";

    public Test() {
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(hexStringToByteArray(messageInString)));
        Test t = new Test();
        t.execute();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public void execute() {
        try {
            Socket s = new Socket("localhost", 42027);
            System.out.println(s.toString());

            OutputStream os = s.getOutputStream();
            os.write(hexStringToByteArray(messageInString));
            os.flush();
            s.close();
//            BufferedReader in = null;
//            PrintWriter out= null;
//            in  = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            out = new PrintWriter(s.getOutputStream(),true);
//            StringBuilder sb  = new StringBuilder();
//            for (byte b : hexStringToByteArray(messageInString)){
//                sb.append((char) b);
//            }
//            String response = sb.toString();
//            out.print(response);
//            out.flush();
////            out.close();
//            System.out.println(s.isClosed());
//            String inputMessage;
//            sb.delete(0,sb.length());
//            System.out.println("sdfjdg");
//            while ((inputMessage = in.readLine()) != null) {
//                for (byte b:inputMessage.getBytes()) sb.append(String.format("%02x", b & 0xFF));
//                String request = sb.toString();
//                System.out.printf("Received message: %s"+'\n',request);
//            }

//            int b=0;
//            InputStream is = s.getInputStream();
//            StringBuilder sb = new StringBuilder();
//            while (b != -1) {
//                b = is.read();
//                sb.append(String.format("%02x", b & 0xFF));
//            }
//            System.out.println("Response message: "+sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
