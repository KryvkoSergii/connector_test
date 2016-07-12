package connectornew;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 05.07.16.
 */
public class ExecutorThread {
    private boolean isBusy;
    private Logger logger = Logger.getLogger(ExecutorThread.class.getClass().getName());

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void process(Socket clientSocket, Map<String, Object> scenario, Map<String, ClientDescriptor> clients) {
        logger.log(Level.INFO, String.format("Thread started "));
        logger.setLevel(Level.ALL);
        int port = -1;
        String address = null;
        long initTime;
//        ServerSocket ss = null;
//        Socket clientSocket = null;
//        ss = new ServerSocket(42027);
//        System.out.println("Waiting...");
//        clientSocket = ss.accept();

        address = clientSocket.getInetAddress().toString();
        port = clientSocket.getPort();
        logger.log(Level.INFO, String.format("Defined address %s:%s", address, port));
        logger.log(Level.INFO, String.format("Accepted %s", clientSocket.getRemoteSocketAddress()));
        initTime = System.currentTimeMillis();
        while (isBusy()) {
            System.out.println("");
            ClientDescriptor clientDescriptor = clients.get("client");
            long initTimeLoadCommand = System.nanoTime();
            ScenarioPairContainer spc = getCommand(scenario, clientDescriptor.getClientState(), 0);
            logger.log(Level.INFO,String.format("Scenario accessing time: %f ms",(double) ((System.nanoTime()-initTimeLoadCommand)*0.000001)));

            if (spc == null) {
                logger.log(Level.INFO, "Scenario executed");
                break;
            }

            logger.log(Level.INFO, String.format("State: %s , command type: %s", Arrays.toString(clientDescriptor.getClientState()), spc.getMethod()));
            switch (spc.getMethod()) {
                //равен методу GET
                case 0: {
                    logger.log(Level.INFO, String.format("GET: Executing command type: GET"));
                    byte[] inputMessage;
                    try {
                        long startRead = System.nanoTime();
                        inputMessage = Transport.read(clientSocket);
                        logger.log(Level.INFO,String.format("Reading time from socket: %f ms",(double) ((System.nanoTime()-startRead)*0.000001)));
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "GET: " + e.getMessage());
                        break;
                    }

                    logger.log(Level.INFO, String.format("GET: Input message in hex: %s", Hex.encodeHexString(inputMessage)));
                    if (spc.getCommand() instanceof String) {
                        //извлекаются переменные из "компилированного" сценария
                        for (VariablesDescriptor varDesc : (List<VariablesDescriptor>) spc.getVariables()) {
                            switch (varDesc.getType()) {
                                // вычленить переменную
                                case 1: {
                                    //запись переменной, извлеченной из полученного от клиента сообщения в ClientDescriptor
                                    clientDescriptor.getVariableContainer()
                                            .put(varDesc.getName(), Arrays.copyOfRange(inputMessage, varDesc.getBeginPosition(), varDesc.getBeginPosition() + varDesc.getLength()));
                                    break;
                                }
                                default: {
                                    logger.log(Level.WARNING, "Unknown command GET");
                                    break;
                                }
                            }
                        }
                        byte[] resultMessage = assemblyMessageInByte(spc, clientDescriptor);
                        logger.log(Level.INFO, String.format("GET: Processed message in hex: %s", Hex.encodeHexString(resultMessage)));
                        logger.log(Level.INFO, String.format("GET: Is received message equals to processed message: %s", Arrays.equals(inputMessage, resultMessage)));
                        break;
                    } else if (spc.getCommand() instanceof byte[]) {
                        byte[] resultMessage = (byte[]) spc.getCommand();
                        logger.log(Level.INFO, String.format("GET: Loaded message in hex from scenario: %s", Hex.encodeHexString(resultMessage)));
                        logger.log(Level.INFO, String.format("GET: Is received message equals to processed message: %s", Arrays.equals(inputMessage, resultMessage)));
                        break;
                    }
                }
                //равен методу PUT
                case 1: {
                    logger.log(Level.INFO, String.format("PUT: Executing command type: PUT"));
                    for (VariablesDescriptor varDesc : (List<VariablesDescriptor>) spc.getVariables()) {
                        if (varDesc.getType() == 3) {
                            //сгенерировать переменную по имени
                            byte[] var = null;
                            if (varDesc.getName().equals("ICMCentralControllerTimer"))
                                var = ByteBuffer.allocate(varDesc.getLength()).putInt((int) System.currentTimeMillis() / 1000).array();
                            clientDescriptor.getVariableContainer().put(varDesc.getName(), var);
                        }
                    }
                    /*проверяет, если команда представлена в сценарии в byte[], она извлекаетя из сценария.
                    в ином случае команда собирается изх переменных и блоков, представленый в byte[]
                    */
                    byte[] resultMessage;
                    if (spc.getCommand() instanceof byte[]) resultMessage = (byte[]) spc.getCommand();
                    else resultMessage = assemblyMessageInByte(spc, clientDescriptor);

                    logger.log(Level.INFO, String.format("PUT: Processed message in hex: %s", Hex.encodeHexString(resultMessage)));
                    try {
                        long startWrite = System.nanoTime();
                        Transport.write(clientSocket, resultMessage);
                        logger.log(Level.INFO,String.format("Writing time to socket: %f ms",(double) ((System.nanoTime()-startWrite)*0.000001)));
                        logger.log(Level.INFO, String.format("PUT: Sent message"));
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "PUT: " + e.getMessage());
                        break;
                    }
                    break;
                }
                default: {
                    logger.log(Level.WARNING, String.valueOf("Unknown command in scenario"));
                    break;
                }
            }
        }
        logger.log(Level.INFO,String.format("Executing time: %s ms",System.currentTimeMillis()-initTime));
        setBusy(false);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ScenarioPairContainer getCommand(Map<String, Object> scenario, String[] state, int level) {
        Iterator iterator = scenario.entrySet().iterator();
        Map.Entry<String, Object> object = null;
        boolean onNextStep = false;
        ScenarioPairContainer spc = null;

        while (iterator.hasNext()) {
            if (!onNextStep) object = (Map.Entry<String, Object>) iterator.next();
            else onNextStep = false;

            if (state[level] == null) state[level] = object.getKey();

            //ссылка на указанный в String[] state ключ элемента
            if (object.getKey().equals(state[level])) {
                //если элемент содержит мап(внутренняя вложеность)
                if (object.getValue() instanceof Map) {
                    spc = getCommand((Map<String, Object>) object.getValue(), state, level + 1);
                    // если spc==null работа с вложенными элементами закончена и необходимо перейти на следующий элемент.
                    if (spc == null) {
                        //если iterator.hasNext()==false более элементов нет, нужно переходить на уровень выше
                        if (!iterator.hasNext()) return null;
                        //переход на следующий элемент. что не выполнять повторного iterator.next() выше введена переменная onNextStep
                        object = (Map.Entry<String, Object>) iterator.next();
                        state[level] = object.getKey();
                        onNextStep = true;
                    }
                    return spc;
                } else {
                    List tmp = (List) object.getValue();
                    //обработка первого прохода по методу
                    if (state[level + 1] == null) state[level + 1] = String.valueOf(0);

                    int positionInList = Integer.valueOf(state[level + 1]);
                    if (tmp.size() < positionInList + 1) return null;
                    spc = (ScenarioPairContainer) tmp.get(positionInList);
                    state[level + 1] = String.valueOf(positionInList + 1);
                    return spc;
                }
            }
        }
        return spc;
    }

    private byte[] assemblyMessageInByte(ScenarioPairContainer spc, ClientDescriptor clientDescriptor) {
        //определение длины и сборка полученненного сообщения
        int messageLength = 0;
        int iterator = 0;
        for (Object arr : spc.getInBytes()) {
            byte[] array = ((byte[]) arr);
            //получение значения переменной из ClientDescriptor
            if (array.length == 0) {
                a:
                for (VariablesDescriptor vd : (List<VariablesDescriptor>) spc.getVariables()) {
                    if (vd.getPositionInArray() == iterator) {
                        messageLength += vd.getLength();
                        break a;
                    }
                }
            } else {
                messageLength += ((byte[]) arr).length;
            }

            iterator++;
        }
        byte[] resultMessage = new byte[messageLength];
        int offset = 0;
        iterator = 0;
        for (Object arr : spc.getInBytes()) {
            byte[] array = ((byte[]) arr);
            //получение значения переменной из ClientDescriptor
            if (array.length == 0) {
                a:
                for (VariablesDescriptor vd : (List<VariablesDescriptor>) spc.getVariables()) {
                    if (vd.getPositionInArray() == iterator) {
                        array = clientDescriptor.getVariableContainer().get(vd.getName());
                        break a;
                    }
                }
            }
            //сборка сообщения
            System.arraycopy(array, 0, resultMessage, offset, array.length);
            offset += array.length;
            iterator++;
        }
        return resultMessage;
    }


}
