package connectornew;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

/**
 * Created by srg on 04.07.16.
 */
public class StartServer {
    private Map<String, Object> scenario;

    public static void main(String[] args) {
//        System.out.println("Init server...");
//
//        ServerSocket ss = new ServerSocket(42027);
//        System.out.println("Waiting...");

        StartServer ss = new StartServer();
        ss.loadScenarioFile();
    }

    private void loadScenarioFile() {
        try {
            Map<String, Object> tmp = ClientDescriptor.parseScenarioContainer("/home/srg/Downloads/scenarios_short1.xml");
            tmp = ClientDescriptor.preCompile(tmp);
            this.scenario = tmp;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

    }

}
