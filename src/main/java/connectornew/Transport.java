package connectornew;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 06.07.16.
 */
@Deprecated
public class Transport {
    private static Logger logger = Logger.getLogger(Transport.class.getClass().getName());

    public static byte[] read(Socket s, boolean shouldWaiting) throws IOException {
        InputStream fromClient = s.getInputStream();
        System.out.println("available " + fromClient.available());

        if (!shouldWaiting && !(fromClient.available() > 0)) {
            logger.log(Level.INFO, "nothing to read");
            return null;
        }

        long messageLength = 0L;
        long messageType = 0L;
        int b;
        byte[] messageLengthInByte = new byte[4];
        byte[] messageTypeInByte = new byte[4];
        // определение длинны сообщения

        int  counter = 0;
        while (counter < messageLengthInByte.length) {
            b = fromClient.read();
            messageLengthInByte[counter] = (byte) b;
            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        messageLength = convertByteArraySize4ToLong(messageLengthInByte);
        //надо ли проверять размер сообщения?
        if (messageLength <= 4329) {
            logger.log(Level.INFO, String.format("message lengths %s - correct", messageLength));
        } else logger.log(Level.INFO, String.format("message lengths %s - incorrect", messageLength));
        // определение типа сообщения
        counter = 0;
        while (counter < messageTypeInByte.length) {
            b = fromClient.read();
            messageTypeInByte[counter] = (byte) b;
            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        messageType = convertByteArraySize4ToLong(messageTypeInByte);
        logger.log(Level.INFO, String.format("message type %s", messageType));
        //формирование сообщения
        int offset = messageLengthInByte.length + messageTypeInByte.length;
        byte[] resultMessage = new byte[(int) messageLength + offset];
        System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
        System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
        //сдвиг, учитывающие начальные сообщения
        counter = 0;
        while (counter < messageLength) {
            b = fromClient.read();
            resultMessage[counter + offset] = (byte) b;
            System.out.print(String.format("%02x", b & 0xFF));
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
