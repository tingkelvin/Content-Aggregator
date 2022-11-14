package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ConcurrentModificationException;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.time.LocalTime;

public class Request extends Thread {
    public final Socket clientSocket;
    public final String requestType;
    public final String serverID;
    // public final String id;
    private ExecutorService pool;
    private LamportClock lamportClock;
    private int curClock;
    private AggregatedContent aggregatedContent;
    private LocalTime time;

    // Constructor
    public Request(Socket socket, String requestType, String serverID,
            LamportClock lamportClock, int curClock, AggregatedContent aggregatedContent, LocalTime localTime) {
        this.clientSocket = socket;
        this.requestType = requestType;
        this.serverID = serverID;
        this.lamportClock = lamportClock;
        this.curClock = curClock;
        this.aggregatedContent = aggregatedContent;
        this.time = localTime;
    }

    public void run() {
        PrintWriter out = null; // get the outputstream of client
        BufferedReader in = null; // get the inputstream of client
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (requestType.equals("PUT")) {
                handlePUT(in, out);
            } else if (requestType.equals("GET")) {
                handleGET(out);
            } else if (requestType.equals("HEARTBEAT")) {
                handleHeartbeat(out);
            } else {
                throw new Exception("Invalid request");
            }
        } catch (Exception e) {
            exceptionnHandler(e.getMessage(), out);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println(serverID + " " + curClock);
            }
        }
    }

    // Handle error
    private void exceptionnHandler(String message, PrintWriter out) {
        if (message.equals("Invalid content format")) {
            sendStatus("500 Internal Server Error", out);
        } else if (message.equals("No content")) {
            sendStatus("204 No Content", out);
        } else if (message.equals("Invalid request")) {
            sendStatus("400 Bad Request", out);
        } else {
            System.out.println(message);
        }
    }

    // Handle Heartbeat
    private void handleHeartbeat(PrintWriter out) {
        lamportClock
                .increment(String.format("Executing %sHEARTBEAT%s request from client %s", Constants.ANSI_RED,
                        Constants.ANSI_RESET, serverID));
        aggregatedContent.setLastSeen(serverID, time);
        sendStatus("200 OK", out);
    }

    // Handle PUT
    private void handlePUT(BufferedReader in, PrintWriter out) throws Exception {
        String line;
        StringBuilder content = new StringBuilder();
        sendStatus("201 Created", out);
        int recievedClock = Integer.parseInt(in.readLine().replaceAll(":", "").split(" ")[1]);

        lamportClock.compareAndSet(recievedClock, "Receiving content.");
        while ((line = in.readLine()) != null) {
            if (line.equals("")) {
                break;
            }
            content.append(line.trim());
            content.append("\n");
        }
        if (content.length() == 0) {
            // this.aggregatedContent.get(curClock).setContent(content, "as");
            aggregatedContent.removeContent(serverID, curClock);
            throw new Exception("No content");
        }

        // System.out.println("WHAT" + " " + content.toString());
        content.setLength(content.length() - 1);
        // this.aggregatedContent.get(curClock).setContent(content, "as");
        this.aggregatedContent.setContent(serverID, curClock, content);

        sendStatus("200 OK", out);
    }

    // Handle GET
    private void handleGET(PrintWriter out) {
        lamportClock.increment("Sending content to " + Constants.ANSI_BLUE + serverID + Constants.ANSI_RESET);
        out.println(String.format("Lamport-lamportClock: %d", lamportClock.getClock()));
        out.println(Constants.XMLHeader);
        try {
            String content = aggregatedContent.getAggregatedContent(curClock, time);
            out.println(Constants.FeedHeader);
            if (content != null) {
                String[] lines = content.split("\n");
                if (lines.length != 0) {
                    for (String line : lines) {
                        out.println(line);
                    }
                }
            }
            out.println(Constants.FeedFooter);
            out.println();

        } catch (ConcurrentModificationException e) {
            System.out.println("\033[4m\033[1m\u001b[31mConcurrent Issue in Get Request. Test did not pass.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("\033[4m\033[1m\u001b[31mConcurrent Issue in Get Request: " + serverID);
            e.printStackTrace();
        }

    }

    private void sendStatus(String status, PrintWriter out) {
        lamportClock.increment("Sending " + Constants.statusColors.get(status) + " to "
                + Constants.getServerColor(requestType, serverID) + ".");
        out.println(status);
        out.println(String.format("Lamport-lamportClock: %d", this.lamportClock.getClock()));
    }

    public int getClock() {
        return curClock;
    }
}
