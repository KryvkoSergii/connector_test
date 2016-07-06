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
public class Transport {
    private static Logger logger = Logger.getLogger(Transport.class.getClass().getName());

    public static byte[] read(Socket s) {
        try {
            InputStream fromClient = s.getInputStream();

            long messageLength, messageType;
            int b;
            byte[] messageLengthInByte = new byte[4];
            byte[] messageTypeInByte = new byte[4];
            // определение длинны сообщения
            int counter = 0;
            while (counter < messageLengthInByte.length) {
                b = fromClient.read();
                messageLengthInByte[counter] = (byte) b;
                counter++;
            }
            messageLength = convertByteArraySize4ToLong(messageLengthInByte);
            logger.log(Level.INFO, String.format("message lengths %s", messageLength));
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
            byte[] resultMessage = new byte[messageLengthInByte.length + messageTypeInByte.length + (int) messageLength];
            System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
            System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
            //сдвиг, учитывающие начальные сообщения
            int offset = messageLengthInByte.length + messageTypeInByte.length;
            counter = 0;
            while (counter < messageLength) {
                b = fromClient.read();
                resultMessage[counter + offset] = (byte) b;
                counter++;
            }
            return resultMessage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(Socket s, byte[] message) {
        try {
            OutputStream toClient = s.getOutputStream();
            toClient.write(message);
            toClient.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static long convertByteArraySize4ToLong(byte[] variable) {
        long value = 0;
        for (int i = 0; i < variable.length; i++) {
            value = (value << 4) + (variable[i] & 0xff);
        }
        return value;
    }
}
