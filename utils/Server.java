package utils;

import java.util.ArrayList;

public class Server extends Thread {
    private String serverID;
    private int timeout = 12;
    private ArrayList<Integer> contentID = new ArrayList<Integer>();
    Thread t;

    public Server(String serverID, int clock) throws Exception {
        this.serverID = serverID;

        this.contentID.add(clock);
    }

    @Override
    public void run() {
        try {
            while (this.timeout != 0) {
                Thread.sleep(1000);
                decrementTimer();
                // System.out.println(String.format("%s time is %d", this.serverID,
                // this.timeout));
                if (this.timeout == 0) {
                    System.out.println(String.format("%s timeout!", this.serverID));

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    synchronized void resetTimer() {
        this.timeout = 12;
    }

    synchronized void decrementTimer() {
        this.timeout--;
    }

    synchronized String getContent() {
        // System.out.println("contenttt" + content.toString());
        return null;
    }

    void startTimer() {
    }
}