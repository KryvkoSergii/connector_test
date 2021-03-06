package connectornew;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
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
    //    private Logger logger = Logger.getLogger(ExecutorThread.class.getClass().getName());
    private Logger logger = Logger.getAnonymousLogger();

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void process(Socket clientSocket, Map<String, Object> scenario, Map<String, ClientDescriptor> clients) {
        logger.log(Level.INFO, String.format("Thread started "));
        logger.setLevel(Level.INFO);
        int port = -1;
        String address = null;
        long initTime;

        address = clientSocket.getInetAddress().toString();
        port = clientSocket.getPort();
        logger.log(Level.INFO, String.format("Defined address %s:%s", address, port));
        logger.log(Level.INFO, String.format("Accepted %s", clientSocket.getRemoteSocketAddress()));

        TransportStack stack = new TransportStack(clientSocket);
        Queue<byte[]> inputMessages = stack.getInputMessages();
        Queue<byte[]> outputMessages = stack.getOutputMessages();
        stack.start();

        initTime = System.currentTimeMillis();
        byte[] inputMessage = null;
        while (isBusy()) {
            //разделитель сообщений
            if (logger.getLevel().intValue() >= Level.INFO.intValue()) System.out.println("");
            //получение сообщений, в том числе и HEARD_BEAT

            ClientDescriptor clientDescriptor = clients.get("client");
            long initTimeLoadCommand = System.nanoTime();
            ScenarioPairContainer spc = getCommand(scenario, clientDescriptor.getClientState(), 0);
            logger.log(Level.INFO, String.format("Scenario accessing time: %f ms", (double) ((System.nanoTime() - initTimeLoadCommand) * 0.000001)));

            if (spc == null) {
                logger.log(Level.INFO, "Scenario executed");
                logger.log(Level.INFO, String.format("Executing time: %s ms", System.currentTimeMillis() - initTime));

                while (outputMessages.size() > 0) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

//                stack.interrupt();
                logger.log(Level.INFO, String.format("Done read cycles %s, write cycles %s", stack.getReadCount(), stack.getWriteCount()));

                /**
                 * на stack не отправлена команда interrupt
                 */
                try {
                    stack.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.exit(0);

                break;
            }

            logger.log(Level.INFO, String.format("State: %s , command type: %s", Arrays.toString(clientDescriptor.getClientState()), spc.getMethod()));
            switch (spc.getMethod()) {
                //метод GET
                case 0: {
                    logger.log(Level.INFO, String.format("GET: Executing command type: GET"));

                    inputMessage = null;
                    long startRead = System.nanoTime();
                    while (inputMessage == null) {
                        inputMessage = inputMessages.poll();
                    }
                    logger.log(Level.INFO, String.format("Reading time from buffer: %f ms", (double) ((System.nanoTime() - startRead) * 0.000001)));
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
                //метод PUT
                case 1: {
                    logger.log(Level.INFO, String.format("PUT: Executing command type: PUT"));
                    for (VariablesDescriptor varDesc : (List<VariablesDescriptor>) spc.getVariables()) {
                        if (varDesc.getType() == 3) {
                            //сгенерировать переменную по имени
                            byte[] var = null;
                            if (varDesc.getName().equals("ICMCentralControllerTimer"))
                                var = ByteBuffer.allocate(varDesc.getLength()).putInt((int) (System.currentTimeMillis() / 1000)).array();
                            clientDescriptor.getVariableContainer().put(varDesc.getName(), var);
                            logger.log(Level.INFO, String.format("TIME IN HEX: " + Hex.encodeHexString(var)));
                        }
                    }
                    /*проверяет, если команда представлена в сценарии в byte[], она извлекаетя из сценария.
                    в ином случае команда собирается изх переменных и блоков, представленый в byte[]
                    */
                    byte[] resultMessage;
                    if (spc.getCommand() instanceof byte[]) {
                        resultMessage = (byte[]) spc.getCommand();
                        logger.log(Level.INFO, String.format("PUT: Processed message in hex: %s", Hex.encodeHexString(resultMessage)));
                    } else {
                        resultMessage = assemblyMessageInByte(spc, clientDescriptor);
                        logger.log(Level.INFO, String.format("PUT: Processed message in hex: %s", Hex.encodeHexString(resultMessage)));
                    }

                    long startWrite = System.nanoTime();
                    outputMessages.add(resultMessage);
                    logger.log(Level.INFO, String.format("Writing time to buffer: %f ms", (double) ((System.nanoTime() - startWrite) * 0.000001)));
                    logger.log(Level.INFO, String.format("PUT: Sent message"));
                    break;
                }
                default: {
                    logger.log(Level.WARNING, String.valueOf("Unknown command in scenario"));
                    break;
                }
            }
        }
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
