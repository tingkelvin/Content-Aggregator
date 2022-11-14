package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {
    public static final String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String FeedHeader = "<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\">";
    public static final String FeedFooter = "</feed>";
    public static final String[] tags = {
            "feed",
            "title",
            "subtitle",
            "link",
            "updated",
            "author",
            "name",
            "email",
            "id",
            "entry",
            "title",
            "link",
            "id",
            "updated",
            "summary",
    };

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static Map<String, String> requestColors = Stream.of(new Object[][] {
            { "PUT", Constants.ANSI_PURPLE + "PUT" + Constants.ANSI_RESET },
            { "GET", Constants.ANSI_CYAN + "GET" + Constants.ANSI_RESET },
            { "HEARTBEAT", Constants.ANSI_RED + "HEARTBEAT" + Constants.ANSI_RESET },
            { "POST", Constants.ANSI_RED + "POST" + Constants.ANSI_RESET }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));

    public static Map<String, String> statusColors = Stream.of(new Object[][] {
            { "400 Bad Request", Constants.ANSI_RED + "400 Bad Request" + Constants.ANSI_RESET },
            { "201 Created", Constants.ANSI_GREEN + "201 Created" + Constants.ANSI_RESET },
            { "200 OK", Constants.ANSI_GREEN + "200 OK" + Constants.ANSI_RESET },
            { "500 Internal Server Error", Constants.ANSI_RED + "500 Internal Server Error" + Constants.ANSI_RESET },
            { "204 No Content", Constants.ANSI_RED + "204 No Content" + Constants.ANSI_RESET },
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));

    public static Object getServerColor(String requestType, String serverID) {
        if (requestType.equals("PUT") || requestType.equals("HEARTBEAT") || requestType.equals("POST"))
            return Constants.ANSI_GREEN + serverID + Constants.ANSI_RESET;
        return Constants.ANSI_BLUE + serverID + Constants.ANSI_RESET;
    }

}