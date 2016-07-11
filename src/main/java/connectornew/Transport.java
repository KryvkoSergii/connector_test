package connectornew;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 06.07.16.
 */
public class Transport {
    private static Logger logger = Logger.getLogger(Transport.class.getClass().getName());

    public static byte[] read(Socket s) throws IOException {
        InputStream fromClient = s.getInputStream();

        long messageLength, messageType;
        int b;
        byte[] messageLengthInByte = new byte[4];
        byte[] messageTypeInByte = new byte[4];
        // определение длинны сообщения
        int counter = 0;
        System.out.println("length");
        while (counter < messageLengthInByte.length) {
            b = fromClient.read();
            messageLengthInByte[counter] = (byte) b;
            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        System.out.println("");
        messageLength = convertByteArraySize4ToLong(messageLengthInByte);
        logger.log(Level.INFO, String.format("message lengths %s", messageLength));
        // определение типа сообщения
        counter = 0;
        System.out.println("type");
        while (counter < messageTypeInByte.length) {
            b = fromClient.read();
            messageTypeInByte[counter] = (byte) b;
            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        System.out.println("");
        messageType = convertByteArraySize4ToLong(messageTypeInByte);
        logger.log(Level.INFO, String.format("message type %s", messageType));
        //формирование сообщения
        byte[] resultMessage = new byte[(int) messageLength];
        System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
        System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
        //сдвиг, учитывающие начальные сообщения
        int offset = messageLengthInByte.length + messageTypeInByte.length;
        counter = 0;
        System.out.println("===BEGIN READ===");
        while (counter < messageLength - offset) {
            b = fromClient.read();
            System.out.print(String.format("%02x", b & 0xFF));
            resultMessage[counter + offset] = (byte) b;
            counter++;
        }
        System.out.println("===END READ===");
        s.setSoLinger(true,0);
//        s.close();
        return resultMessage;
    }

    public static void write(Socket s, byte[] message) throws IOException {
        OutputStream toClient = s.getOutputStream();
        toClient.write(message);
        System.out.println("===BEGIN WRITE===");
        System.out.println(Arrays.toString(message));
        System.out.println("===END WRITE===");
        toClient.flush();
        s.setSoLinger(true,0);
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        s.close();

    }

    private static long convertByteArraySize4ToLong(byte[] variable) {
        long value = 0;
        for (int i = 0; i < variable.length; i++) {
            value = (value << 4) + (variable[i] & 0xff);
        }
        return value;
    }
}
