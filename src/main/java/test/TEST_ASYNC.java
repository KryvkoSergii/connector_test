package test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Created by srg on 18.07.16.
 */
public class TEST_ASYNC {
    private static String messageInString = "0000005600000003001000100000000f7fffffff00001388001c02960fffffff000003ff00000003000000000000000000000000010c4354494f5353657276657200020c4354494f5353657276657200030c4354494f";


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public static void main(String[] args) {
        int clPrt = 10501;
        SocketChannel sc = null;
        Selector sel = null;
        try {
            sc = SocketChannel.open();
            sel = Selector.open();

            sc.configureBlocking(false);
            sc.socket().bind(new InetSocketAddress(clPrt));
            sc.register(sel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            boolean done = false, written = false;
            int i = 0;

            Charset cs = Charset.forName("utf8");
            ByteBuffer buf = ByteBuffer.allocate(16);
            while (!done) {
                sel.select();
                Iterator it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    sc = (SocketChannel) key.channel();
                    if (key.isConnectable() && !sc.isConnected()) {
                        InetAddress addr = InetAddress.getByName(null);
                        boolean success = sc.connect(new InetSocketAddress(
                                addr, 42027));
                        if (!success)
                            sc.finishConnect();
                    }

                    if (key.isReadable()) {
                        System.out.println(sc.read((ByteBuffer) buf));
//                        if (sc.read((ByteBuffer) buf.clear()) > 0) {
//                            String response = cs
//                                    .decode((ByteBuffer) buf.flip()).toString();
//                            System.out.print(response);
//                            if (response.indexOf("END") != -1)
//                                done = true;
//                            buf.flip()
//                        }
                    }

                    if (key.isWritable()) {
//                        if (i < 10)
//                            sc.write(ByteBuffer.wrap(new String("howdy " + i
//                                    + 'n').getBytes()));
//                        else if (i == 10)
//                            sc.write(ByteBuffer.wrap(new String("ENDn")
//                                    .getBytes()));
//                        written = true;
//                        i++;
                        sc.write(ByteBuffer.wrap(hexStringToByteArray(messageInString)));
                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                sc.close();
                sel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
