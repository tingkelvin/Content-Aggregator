import utils.XMLWriter;
import utils.LamportClock;
import utils.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ContentServer extends TimerTask {
    private String serverID;
    private boolean alive = false;
    Timer timer;
    private String ASAddress;
    private int ASPort;
    LamportClock clock;

    public ContentServer(String serverID, String ASAddress, int ASPort, boolean verbose) {
        this.serverID = serverID;
        this.ASAddress = ASAddress;
        this.ASPort = ASPort;
        this.clock = new LamportClock(serverID, 10, verbose);
    }

    private void sendContent(String file, int delay, boolean heartbeat, boolean badReq) throws InterruptedException {
        try {
            Socket socket = new Socket(ASAddress, ASPort);
            PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int content_length = 0;
            String xmlContent = XMLWriter.convert(file);
            if (xmlContent != null) {
                content_length = xmlContent.getBytes("UTF-8").length;
            }

            String fileName = "/" + file.substring(0, file.indexOf('.')) + ".xml";
            if (badReq) {
                clock.increment("Sending POST request.");
                // Follow the HTTP protocol of POST <path> HTTP/1.1
                out.println("POST " + fileName + " HTTP/1.1");
            } else {
                clock.increment("Sending PUT request.");
                // Follow the HTTP protocol of PUT <path> HTTP/1.1
                out.println("PUT " + fileName + " HTTP/1.1");
            }
            out.println("User-Agent: ATOMClient/1/0");
            out.println("Content-Type: text/xml");
            out.println(String.format("Content-Length: %d", content_length));
            out.println(String.format("serverID: %s", this.serverID));
            out.println(String.format("requestID: %s%s", this.serverID, fileName));
            out.println(String.format("Lamport-Clock: %d", this.clock.getClock()));
            out.println();

            String line;
            boolean lastline = false;
            while ((line = in.readLine()) != null && lastline != true) {
                String reply = line;
                if (reply.equals("201 Created")) {
                    String recievedClock = in.readLine().replaceAll(":", "").split(" ")[1];
                    clock.compareAndSet(Integer.parseInt(recievedClock), reply + " recived.");
                    clock.increment("Sending content after " + Constants.ANSI_RED + delay / 1000 + " seconds."
                            + Constants.ANSI_RESET);
                    out.println(String.format("Lamport-Clock: %d", this.clock.getClock()));

                    // Delay uploading
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    if (xmlContent != null) {
                                        out.println(xmlContent);
                                    }
                                    out.println();
                                }
                            },
                            delay);
                } else {
                    lastline = true;
                    String recievedClock = in.readLine().replaceAll(":", "").split(" ")[1];
                    clock.compareAndSet(Integer.parseInt(recievedClock),
                            Constants.statusColors.get(reply) + " recived.");
                    if (reply.equals("200 OK")) {
                        if (!alive && heartbeat) {
                            this.alive = true;
                            heartbeat();
                        }
                    }

                    // Retry
                    else if (reply.equals("500 Internal Server Error")) {
                        clock.increment(
                                this.serverID + fileName
                                        + "is not done due to internal server error. Re-trying after 5 seconds.");
                        TimeUnit.SECONDS.sleep(5);
                        sendContent("content10.txt", delay, heartbeat, false);
                    }

                    else if (reply.equals("204 No Content")) {
                        clock.increment(this.serverID + fileName
                                + "is not done due to no content. Re-trying after 5 seconds.");
                        TimeUnit.SECONDS.sleep(5);
                        sendContent("content11.txt", delay, heartbeat, false);
                    }

                    else if (reply.equals("400 Bad Request")) {
                        clock.increment(this.serverID + fileName
                                + "is not done due to bad request. Re-trying after 5 seconds.");
                        TimeUnit.SECONDS.sleep(5);
                        sendContent("content12.txt", delay, heartbeat, false);
                    }

                }

            }

        } catch (ConnectException e) {
            TimeUnit.SECONDS.sleep(5);
            System.out.println("retry ");
            sendContent(file, delay, heartbeat, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // System.out.println(header + "Sending content request is done.");
        }
    }

    private void heartbeat() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            Socket socket;

            @Override
            public void run() {
                try {
                    socket = new Socket(ASAddress, ASPort);
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    clock.increment("Sending Heartbeat");
                    out.println("HEARTBEAT HTTP/1.1");
                    out.println(String.format("serverID: %s", serverID));
                    out.println(String.format("Lamport-Clock: %d", clock.getClock()));
                    out.println();
                    String reply = in.readLine();
                    String recievedClock = in.readLine().replaceAll(":", "").split(" ")[1];
                    clock.compareAndSet(Integer.parseInt(recievedClock), reply + " recived.");

                } catch (Exception e) {
                    // e.printStackTrace();
                }

            }
        }, 0, 10000);

    }

    @Override
    public void run() {

    };

    public static void main(String[] args) throws NumberFormatException, InterruptedException {
        String[] host = args[0].split(":");
        String serverID = args[1];
        String file = args[2];
        String delay = args[3];
        boolean heartbeat = Boolean.parseBoolean(args[4]);
        boolean badReq = Boolean.parseBoolean(args[5]);
        boolean verbose = Boolean.parseBoolean(args[6]);
        ContentServer cs = new ContentServer(serverID, host[0], Integer.parseInt(host[1]), verbose);
        cs.sendContent(file, Integer.parseInt(delay) * 1000, heartbeat, badReq);
    }
}