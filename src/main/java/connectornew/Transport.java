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
        long messageLength = 0L;
        long messageType = 0L;
        int b;
        byte[] messageLengthInByte = new byte[4];
        byte[] messageTypeInByte = new byte[4];
        // определение длинны сообщения
        boolean correct = false;
        int counter;
        while (!correct) {
            counter = 0;
            while (counter < messageLengthInByte.length) {
                b = fromClient.read();
                messageLengthInByte[counter] = (byte) b;
//                System.out.print(String.format("%02x", b & 0xFF));
                counter++;
            }
            messageLength = convertByteArraySize4ToLong(messageLengthInByte);
            if (messageLength <= 4329) {
                correct = true;
                logger.log(Level.INFO, String.format("message lengths %s - correct", messageLength));
            } else logger.log(Level.INFO, String.format("message lengths %s - incorrect", messageLength));
        }
        // определение типа сообщения
        counter = 0;
        while (counter < messageTypeInByte.length) {
            b = fromClient.read();
            messageTypeInByte[counter] = (byte) b;
            counter++;
        }
        messageType = convertByteArraySize4ToLong(messageTypeInByte);
        logger.log(Level.INFO, String.format("message type %s", messageType));
        //формирование сообщения
        int offset = messageLengthInByte.length + messageTypeInByte.length;
        byte[] resultMessage = new byte[(int) messageLength+offset];
        System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
        System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
        //сдвиг, учитывающие начальные сообщения
        counter = 0;
        while (counter < messageLength) {
            b = fromClient.read();
            resultMessage[counter + offset] = (byte) b;
            counter++;
        }
        s.setSoLinger(true, 0);
        return resultMessage;
    }

    public static void write(Socket s, byte[] message) throws IOException {
        OutputStream toClient = s.getOutputStream();
        toClient.write(message);
        toClient.flush();
        s.setSoLinger(true, 0);
    }

    private static long convertByteArraySize4ToLong(byte[] variable) {
        long value = 0;
        for (int i = 0; i < variable.length; i++) {
            value = (value << 4) + (variable[i] & 0xff);
        }
        return value;
    }
}
