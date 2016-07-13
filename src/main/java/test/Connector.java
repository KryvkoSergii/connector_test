package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by srg on 29.06.16.
 */
public class Connector {

    public static void main(String[] args) {
        try {
            boolean isInterrupted = false;
            ServerSocket ss = new ServerSocket(42027);
            System.out.println("Waiting...");

            while(!isInterrupted) {
                Socket s = ss.accept();
                System.out.println("Connected: " + s.getRemoteSocketAddress());
                InputStream stream = s.getInputStream();
                OutputStream outputStream = s.getOutputStream();
                int b = 0;
                StringBuilder sb = new StringBuilder();

                long messageLength = 0;
                while (sb.length() < 8) {
                    b = stream.read();
                    sb.append(String.format("%02x", b & 0xFF));
                    messageLength = Long.parseLong(sb.toString(), 16);
                    System.out.println(sb.toString());
                }

                sb.delete(0, sb.length());
                long messageType = 0;
                while (sb.length() < 8) {
                    b = stream.read();
                    sb.append(String.format("%02x", b & 0xFF));
                    messageType = Long.parseLong(sb.toString(), 16);
                    System.out.println(sb.toString());
                }
                System.out.printf("messageLength = %s messageType = %s" + '\n', messageLength, messageType);

                sb.delete(0, sb.length());
                long iterator = 0;
                while (iterator < messageLength) {
                    b = stream.read();
                    sb.append(String.format("%02x", b & 0xFF));
                    iterator++;
                }
                String request = sb.toString();
                System.out.printf("Received message: %s" + '\n', request);
                String InvokeIDString = getVariable(0, 8, request);
                System.out.println("InvokeIDString = " + InvokeIDString);
                Integer InvokeIDInteger = Integer.parseInt(InvokeIDString, 16);

                String responseTemplate = "000000280000000400000004001c029600000000000000045746cc1e000000110009e4020001d00400001388e0020000";
                StringBuilder responseSB = new StringBuilder();
                responseSB.append(getVariable(0, 15, responseTemplate));
                responseSB.append(String.format("%08x", ++InvokeIDInteger));
                responseSB.append(getVariable(24, 24, responseTemplate));
                responseSB.append(String.format("%08x", (int) System.currentTimeMillis() / 1000));
                responseSB.append(responseTemplate.substring(56, responseTemplate.length() - 1));
                String response = responseSB.toString();
                System.out.printf("Response message: %s" + '\n', response);
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
                outputStream.write(hexStringToByteArray(request));
                System.out.println("sent");
                s.close();
            }
            System.out.println("end");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getVariable(int position,int length, String message){
        String result = message.substring(position,position+length);
        return result;
    }

//    private long readLongVariableFromInputStream(InputStream stream, int length){
//        byte b;
//        while (sb.length()<8) {
//            b = stream.read();
//            sb.append(String.format("%02x", b & 0xFF));
//            System.out.println(String.format("%02x", b & 0xFF));
//            messageLength = Long.parseLong(sb.toString(),16);
//        }
//
//        return
//    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
