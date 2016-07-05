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
    private Map<String, Object> scenario;
    private Map<String, ClientDescriptor> clients = new ConcurrentHashMap<String, ClientDescriptor>();
    private List<ExecutorThread> threadsPool = new ArrayList<ExecutorThread>();
    private boolean isStopped = false;


    //getter and setters
    public Map<String, ClientDescriptor> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientDescriptor> clients) {
        this.clients = clients;
    }


    //methods
    public static void main(String[] args) {
        StartServer ss = new StartServer();
        ss.loadScenarioFile("/home/srg/Downloads/scenarios_short1.xml");
        ss.createExecutorsPool(1);
        ss.getClients().put("client",new ClientDescriptor());
//        ss.startListening();
        ss.test();
    }

    private void test(){
        ExecutorThread et = threadsPool.get(0);
        et.process(null,scenario,clients);
        et.process(null,scenario,clients);
    }

    private void startListening() {
        logger.log(Level.INFO, String.format("Init server acceptor..."));
        try {
            ServerSocket ss = new ServerSocket(42027);
            while (!isStopped) {
                System.out.println("Waiting...");
                Socket s = ss.accept();
                for (ExecutorThread e : threadsPool) {
                    if (!e.isBusy()) {
                        e.setBusy(true);
                        e.process(s,scenario,clients);
                        continue;
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
            logger.log(Level.INFO, String.format("Loading scenario from file: %s", scenarioFilePath));
            Map<String, Object> tmp = ClientDescriptor.parseScenarioContainer(scenarioFilePath);
            tmp = ClientDescriptor.preCompile(tmp);
            this.scenario = tmp;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }

    }

}
