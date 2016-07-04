package connectornew;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by srg on 04.07.16.
 */
public class ClientDescriptor {
    Map<String, String> variableContainer = new ConcurrentHashMap<String, String>();
    Map<String, List<ScenarioPairContainer>> scenarioContainer;


    public static Map<String, Object> parseScenarioContainer(String scenarioFilePath) throws ParserConfigurationException,
            IOException, SAXException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(scenarioFilePath));
        Map<String, Object> result = getSubNode(doc, 0);
        return result;
    }

    private static Map<String, Object> getSubNode(Node node, int level) {
        Map<String, Object> tmp = new HashMap<String, Object>();
        NodeList list = node.getChildNodes();


        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i); // текущий нод
            if (currentNode.hasChildNodes() && currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //проверка есть ли внутренние put/get
//                String name = currentNode.getNodeName();
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

}
