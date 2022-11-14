package utils;

import java.io.Serializable;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.Constants;

public class Content implements Serializable {
    private String serverID;
    private String content = null;
    private boolean isReady = false;
    private boolean isEmpty = true;
    private int curClock;

    public Content(String serverID2, int curClock) {
        this.serverID = serverID2;
        this.curClock = curClock;

    }

    // If content is uploaded, notify any incoming request
    synchronized boolean setContent(StringBuilder content2, String s) throws Exception {
        if (content2.length() != 0) {
            validate(content2.toString());
            content2 = strip(content2);
            this.content = content2.toString();
            this.isEmpty = false;
        }
        this.isReady = true;
        notifyAll();
        return !isEmpty;
    }

    // Wait until content is finished uploading
    public synchronized String getContent() {
        while (!isReady) {
            try {
                System.out.println(Constants.ANSI_YELLOW + "Aggregator Server" + Constants.ANSI_RESET
                        + " : Waiting content to be uploaded.");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread Interrupted");
            }
        }
        if (!isEmpty) {
            return content;
        } else {
            return null;
        }
    }

    // Check if the content is valid
    private void validate(String content) throws Exception {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(content));
            Document document = builder.parse(is);
            NodeList nodeList = document.getElementsByTagName("*");
            int j = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (!node.getNodeName().equals(Constants.tags[j])
                            || node.getFirstChild().getTextContent().equals("")) {
                        throw new Exception("Invalid content format");
                    }
                    if (node.getNodeName().equals("summary")) {
                        j = 8;
                    }
                }
                j++;
            }
        } catch (Exception e) {
            isReady = true;
            throw new Exception("Invalid content format");
        }
    }

    private StringBuilder strip(StringBuilder content) {
        content.delete(0, content.indexOf("\n") + 1);
        content.delete(0, content.indexOf("\n") + 1);
        content.delete(content.lastIndexOf("\n"), content.length());
        return content;
    }

    synchronized public void setReady() {
        isReady = true;
    }

    public int getClock() {
        return curClock;
    }
}
