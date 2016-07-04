package connectornew;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by srg on 04.07.16.
 */
public class ClientDescriptor {
    Map<String, String> variableContainer;
    Map<String, Object> scenarioContainer;
    private String[] clientState;

    //Methods
    //Static methods
    public static Map<String, Object> parseScenarioContainer(String scenarioFilePath) throws ParserConfigurationException,
            IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(scenarioFilePath));
        Map<String, Object> result = getSubNode(doc, 0);
        return result;
    }

    private static Map<String, Object> getSubNode(Node node, int level) {
        Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i); // текущий нод
            if (currentNode.hasChildNodes() && currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //проверка есть ли внутренние put/get
                String name = currentNode.getChildNodes().item(1).getNodeName();
                if (name.equals("GET") || name.equals("PUT")) {
                    tmp.put(currentNode.getNodeName(), getPair(currentNode.getChildNodes()));
                } else {
                    tmp.put(currentNode.getNodeName(), getSubNode(currentNode, level + 1));
                }
            }
        }
        return tmp;
    }

    private static List<ScenarioPairContainer> getPair(NodeList list) {
        List<ScenarioPairContainer> tmp = new ArrayList<ScenarioPairContainer>();
        byte b;
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                b = (byte) (n.getNodeName().equals("GET") ? 0 : 1);
                tmp.add(new ScenarioPairContainer(b, n.getChildNodes().item(0).getNodeValue()));
            }
        }
        return tmp;
    }



    //Getters and setters
    public Map<String, String> getVariableContainer() {
        return variableContainer;
    }

    public void setVariableContainer(Map<String, String> variableContainer) {
        this.variableContainer = variableContainer;
    }

    public Map<String, Object> getScenarioContainer() {
        return scenarioContainer;
    }

    public void setScenarioContainer(Map<String, Object> scenarioContainer) {
        this.scenarioContainer = scenarioContainer;
    }

    public String[] getClientState() {
        return clientState;
    }

    public void setClientState(String[] clientState) {
        this.clientState = clientState;
    }

    /**
     * просматривает сценарий на наличие неизменяемых значений GET/PUT, пересоздает Map<String, Object> с представлением значений
     * GET/PUT в виде массива байт.
     *
     * @return
     */
    public static Map<String, Object> preCompile(Map<String, Object> rawScenario) {
        Set<Map.Entry<String, Object>> root = rawScenario.entrySet();
        for (Map.Entry<String, Object> m : root) {
            if (m.getValue() instanceof Map) {
                preCompile((Map) m.getValue());
            } else if (m.getValue() instanceof List) {
                for (ScenarioPairContainer spc : (List<ScenarioPairContainer>) m.getValue()) {
                    if (!(spc.getCommand() instanceof byte[])) {
                        spc.setCommand(hexStringToByteArray(spc.getCommand().toString()));
                    }
                }
            } else try {
                throw new ConnectorException("Scenario casting exception");
            } catch (ConnectorException e) {
                System.out.println(e.getMessage());
            }
        }
        return rawScenario;
    }

    public static byte[] hexStringToByteArray(String hexInString) {
        int len = hexInString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexInString.charAt(i), 16) << 4) + Character.digit(hexInString.charAt(i + 1), 16));
        }
        return data;
    }

}
