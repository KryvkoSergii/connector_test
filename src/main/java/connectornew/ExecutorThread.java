package connectornew;

import org.apache.commons.codec.binary.Hex;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 05.07.16.
 */
public class ExecutorThread {
    //    private Socket clientSocket;
    private boolean isBusy;
    //    private ClientDescriptor clientDescriptor;
    private Logger logger = Logger.getLogger(ExecutorThread.class.getClass().getName());

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void process(Socket clientSocket, Map<String, Object> scenario, Map<String, ClientDescriptor> clients) {
        logger.log(Level.INFO, String.format("Accepted %s", clientSocket.getRemoteSocketAddress()));
        ClientDescriptor clientDescriptor = clients.get("client");
        ScenarioPairContainer spc = getCommand(scenario, clientDescriptor.getClientState(), 0);

        switch (spc.getMethod()) {
            //равен методу GET
            case 0: {
                byte[] inputMessage = null;
                if (spc.getCommand() instanceof String) {
                    inputMessage = Transport.read(clientSocket);
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
                            default:
                                logger.log(Level.WARNING, "Unknown command");
                                break;
                        }
                    }
                }

                logger.log(Level.INFO,String.format("Is received message equals to processed message: %s",assemblyMessageInByte(spc).equals(inputMessage)));
                break;
            }
            //равен методу PUT
            case 1: {
                for (VariablesDescriptor varDesc : (List<VariablesDescriptor>) spc.getVariables()) {
                    switch (varDesc.getType()) {
                        case 2: {
                            //вставить переменную из массива в ClientDescriptor
                            spc.getInBytes().set(varDesc.getPositionInArray(), clientDescriptor.getVariableContainer().get(varDesc.getName()));
                            break;
                        }
                        case 3: {
                            //сгенерировать переменную по имени
                            byte[] var = null;
                            if (varDesc.getName().equals("ICMCentralControllerTimer"))
                                var = ByteBuffer.allocate(varDesc.getLength()).putInt((int) System.currentTimeMillis() / 1000).array();
                            spc.getInBytes().set(varDesc.getPositionInArray(), var);
                            break;
                        }
                        default:
                            logger.log(Level.WARNING, "Unknown command");
                            break;
                    }
                }
                byte[] resultMessage = assemblyMessageInByte(spc);
                logger.log(Level.INFO,String.format("Processed message in hex: %s", Hex.encodeHexString(resultMessage)));
                Transport.write(clientSocket,resultMessage);
                break;
            }
            default: {
                logger.log(Level.WARNING, String.valueOf("Unknown command in scenario"));
                break;
            }
        }
        setBusy(false);
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

    private byte[] assemblyMessageInByte(ScenarioPairContainer spc){
        //определение длины и сборка полученненного сообщения
        int messageLength = 0;
        for(Object arr : spc.getInBytes()){
            messageLength += ((byte[]) arr).length;
        }
        byte[] resultMessage = new byte[messageLength];
        int offset = 0;
        for(Object arr : spc.getInBytes()){
            byte[] array = ((byte[]) arr);
            System.arraycopy(array,0,resultMessage,offset,array.length);
            offset += array.length;
        }
        return resultMessage;
    }

}
