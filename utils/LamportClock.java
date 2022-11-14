package utils;

import utils.Constants;

public class LamportClock {
    private volatile int clock;
    private String serverID;
    private String serverMsg;
    private boolean verbose;

    public LamportClock(String serverID, int num, boolean verbose) {
        String[] host = serverID.split("-");
        this.verbose = verbose;
        if (host[0].equals("Aggregator")) {
            serverMsg = String.format(Constants.ANSI_YELLOW + "Aggregator-Server" + Constants.ANSI_RESET + ": ");
        } else if (host[0].equals("Content")) {
            serverMsg = String.format(Constants.ANSI_GREEN + serverID + Constants.ANSI_RESET + ": ");
        } else {
            serverMsg = String.format(Constants.ANSI_BLUE + serverID + Constants.ANSI_RESET + ": ");
        }
        this.serverID = serverID;
        this.clock = num;
    }

    public synchronized void increment(String message) {
        int tmp = getClock();
        this.clock++;
        if (verbose)
            System.out.println(
                    String.format(serverMsg + message + " Increase Lamport Clock By 1: %s %d -> %d %s",
                            Constants.ANSI_BLUE,
                            tmp, getClock(), Constants.ANSI_RESET));
    }

    public synchronized int getClock() {
        return this.clock;
    }

    public synchronized int compareAndSet(int recievedClock, String message) {
        int c = Math.max(recievedClock, getClock());
        setClock(c, message);
        increment(message);
        return getClock();
    }

    public synchronized void setClock(int c, String message) {
        this.clock = c;
    }

}
