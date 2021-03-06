package connectornew;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 04.07.16.
 */
public class StartServer {
    private Logger logger = Logger.getLogger(StartServer.class.getClass().getName());
    private Map<String, Object> scenarioInitiateConnection;
    private Map<String, ClientDescriptor> clients = new ConcurrentHashMap<String, ClientDescriptor>();
    private List<ExecutorThread> threadsPool = new ArrayList<ExecutorThread>();
    private boolean isStopped = false;

    //methods
    public static void main(String[] args) {
        StartServer ss = new StartServer();
        ss.doLabel();
        // загрузка сценария
        ss.loadScenarioFile("/home/srg/java/Idea-WorkSpaces/CTI/connector_test/src/main/resources/scenarios_short1.xml");
//        if (args.length>0 && args[0] != null && !args[0].isEmpty()) ss.loadScenarioFile(args[0].toString());
//        else ss.loadScenarioFile("/home/user/tmp/scenarios_short1.xml");
        //установка количества исполнительных потоков
        ss.createExecutorsPool(1);
        ss.getClients().put("client", new ClientDescriptor());
        ss.startListening();
//        ss.test();
    }

    //getter and setters
    public Map<String, ClientDescriptor> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientDescriptor> clients) {
        this.clients = clients;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void test() {
        //
        ExecutorThread et = threadsPool.get(0);
        et.process(null, scenarioInitiateConnection, clients);
        et.process(null, scenarioInitiateConnection, clients);
    }

    private void startListening() {
        logger.log(Level.INFO, String.format("Init server acceptor..."));
        try {
            ServerSocket ss = new ServerSocket(42027);
            while (!isStopped) {
                logger.log(Level.INFO,"Waiting...");
                Socket s = ss.accept();
                s.setSendBufferSize(4096);
                for (ExecutorThread e : threadsPool) {
                    if (!e.isBusy()) {
                        e.setBusy(true);
                        e.process(s, scenarioInitiateConnection, clients);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void createExecutorsPool(int initSize) {
        for (int i = 0; i < initSize; i++) threadsPool.add(new ExecutorThread());
        logger.log(Level.INFO, String.format("created %s threads", threadsPool.size()));
    }

    private void loadScenarioFile(String scenarioFilePath) {
        try {
            long initTime = System.currentTimeMillis();
            logger.log(Level.INFO, String.format("Loading scenarioInitiateConnection from file: %s", scenarioFilePath));
            Map<String, Object> tmp = ClientDescriptor.parseScenarioContainer(scenarioFilePath);
            tmp = ClientDescriptor.preCompile(tmp);
            this.scenarioInitiateConnection = tmp;
            logger.log(Level.INFO,String.format("Script preparing time: %s ms",System.currentTimeMillis()-initTime));
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }

    }

    private void doLabel() {
        System.out.println("*****   *******     **   **     **   **     *****   *****   ******  *******     *******");
        System.out.println("*****   *******     **   **     **   **     *****   *****   ******  *******     *******");
        System.out.println("**      **   **     ***  **     ***  **     **      **        **    **   **     **   **");
        System.out.println("**      **   **     ** * **     ** * **     *****   **        **    **   **     *******");
        System.out.println("**      **   **     **  ***     **  ***     **      **        **    **   **     **  ** ");
        System.out.println("*****   *******     **   **     **   **     *****   *****     **    *******     **   **");
        System.out.println("*****   *******     **   **     **   **     *****   *****     **    *******     **   **");

    }

}
