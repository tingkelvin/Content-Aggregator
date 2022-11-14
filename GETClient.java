import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import utils.LamportClock;
import utils.Constants;
import utils.FileIO;

public class GETClient {
    private String serverID;
    private LamportClock clock;

    public GETClient(String serverID, boolean verbose, int c) {
        this.serverID = serverID;
        clock = new LamportClock(serverID, c, verbose);
    }

    private void getContent(String ASAddress, int ASPort) {
        try {
            Socket socket = new Socket(ASAddress, ASPort);
            PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Follow the HTTP protocol of PUT <path> HTTP/1.1
            clock.increment("Sending get request");
            out.println("GET " + "/feed.xml" + " HTTP/1.1");
            out.println("User-Agent: ATOMClient/1/0");
            out.println(String.format("serverID: %s", this.serverID));
            out.println(String.format("requestID: %s%s", this.serverID, "clinetID1"));
            out.println(String.format("Lamport-Clock: %d", clock.getClock()));
            out.println();

            String line;
            StringBuilder xmlString = new StringBuilder();
            int recievedClock = Integer.parseInt(in.readLine().replaceAll(":", "").split(" ")[1]);
            clock.compareAndSet(recievedClock, "Receiving content.");
            while ((line = in.readLine()) != null) {
                if (line.equals("")) {
                    break;
                }
                xmlString.append(line);
            }

            clock.compareAndSet(recievedClock, "Content Recieved.");

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString.toString()));
            Document document = builder.parse(is);
            NodeList nodeList = document.getElementsByTagName("*");
            boolean hasContent = false;
            StringBuilder pasredContent = new StringBuilder();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("feed") || nodeName.equals("author"))
                        continue;
                    if (nodeName.equals("entry")) {
                        hasContent = true;
                        pasredContent.append("entry" + "\n");
                        continue;
                    }
                    if (nodeName.equals("name")) {
                        pasredContent.append("author" + node.getFirstChild().getTextContent() + "\n");
                        continue;
                    }
                    String tmp = nodeName + node.getFirstChild().getTextContent();
                    pasredContent.append(tmp + "\n");

                }
            }

            FileIO.writeTxt(serverID, pasredContent.toString());
            // writer.close();
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String[] host = args[0].split(":");
        String[] serverID = args[1].split("-");
        boolean verbose = Boolean.parseBoolean(args[2]);
        GETClient c = new GETClient(args[1], verbose, Integer.parseInt(serverID[1]));
        c.getContent(host[0], Integer.parseInt(host[1]));
    }
}