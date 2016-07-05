package connectornew;

import java.net.Socket;
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
//        logger.log(Level.INFO, String.format("Accepted %s", clientSocket.getRemoteSocketAddress()));
        ClientDescriptor clientDescriptor = clients.get("client");
        ScenarioPairContainer spc = getCommand(scenario, clientDescriptor.getClientState(), 0);
        //равен методу GET
        switch (spc.getMethod()) {
            case 0: {
                if (spc.getCommand() instanceof String) {

                }
            }
            case 1: {
            }
            default: {
                logger.log(Level.WARNING, String.valueOf("Unknown command in scenario"));
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

    private void processVariables(String command, Map<String, String> variableContainer) {
        if (command.contains("#")) {
            StringTokenizer st = new StringTokenizer(command,":");
            String
            while (st.hasMoreTokens()){

            }
            st.nextToken().contains("#")


            //
            int begin command.indexOf("#{");
        }
    }

}
