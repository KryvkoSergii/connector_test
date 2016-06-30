package test;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by srg on 29.06.16.
 */
public class Connector {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(42027);
            System.out.println("Waiting...");
            Socket s = ss.accept();
            System.out.println("Connected: " + s.getRemoteSocketAddress());
            InputStream stream = s.getInputStream();
            int b = 0;
            StringBuilder sb = new StringBuilder();
            long messageLength = 0;
            while (sb.length()<8) {
                b = stream.read();
                sb.append(String.format("%02x", b & 0xFF));
                System.out.println(String.format("%02x", b & 0xFF));
                messageLength = Long.parseLong(sb.toString(),16);
            }
            long iterator = 0;
            while (iterator < messageLength-1) {
                b = stream.read();
                sb.append(String.format("%02x", b & 0xFF));
                System.out.println(String.format("%02x", b & 0xFF));
                iterator++;
            }
            String request = sb.toString();
            System.out.printf("Received message: %s"+'\n',request);
            String InvokeIDString = getVariable(16,8,request);
            System.out.println("InvokeIDString = " + InvokeIDString);
            Integer InvokeIDInteger = Integer.parseInt(InvokeIDString,16);

            String responseTemplate = "000000280000000400000004001c029600000000000000045746cc1e000000110009e4020001d00400001388e0020000";
            StringBuilder responseSB = new StringBuilder();
            responseSB.append(getVariable(0,15,responseTemplate));
            responseSB.append(String.format("%08x", ++InvokeIDInteger));
            responseSB.append(getVariable(24,24,responseTemplate));
            responseSB.append(String.format("%08x", (int) System.currentTimeMillis()/1000));
            responseSB.append(responseTemplate.substring(56,responseTemplate.length()-1));
            String response = responseSB.toString();
            System.out.printf("Response message: %s"+'\n',responseSB.toString());

//            OutputStream os = s.getOutputStream();
//            os.write(hexStringToByteArray(response));

//            while (b != -1) {
//                b = stream.read();
//                sb.append(String.format("%02x", b & 0xFF));
////                if (iteraror<messageLenght.length){
////                    messageLenght[iteraror] = b;
////                    iteraror++;
////                }
////                if (!finished && iteraror==messageLenght.length) {
////                    finished = true;
////                    System.out.println();
////                    System.out.println("length "+ Arrays.toString(messageLenght));}
//            }
            System.out.println("closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getVariable(int position,int length, String message){
        String result = message.substring(position,position+length);
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
