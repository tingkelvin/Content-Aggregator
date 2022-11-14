package utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import utils.Content;

public class AggregatedContent implements Serializable {
    private Map<Integer, Content> aggregatedContent = new ConcurrentHashMap<Integer, Content>();
    private ConcurrentHashMap<Integer, LocalTime> lastSeen = new ConcurrentHashMap<>();
    private HashMap<String, Set<Integer>> serverPool = new HashMap<>();

    public AggregatedContent() throws Exception {
    }

    public void addContent(String serverID, int clock, LocalTime time) {
        aggregatedContent.put(clock, new Content(serverID, clock));
        lastSeen.put(clock, time);
    }

    // Store which server upload the content
    public void setContent(String serverID, int curClock, StringBuilder content) throws Exception {
        if (aggregatedContent.get(curClock).setContent(content, "AS")) {
            replicate("aggregatedContent", this);
            if (serverPool.containsKey(serverID)) {
                serverPool.get(serverID).add(curClock);

            } else {
                Set<Integer> set = new HashSet<>();
                set.add(curClock);
                serverPool.put(serverID, set);
            }
        }
    }

    public String getAggregatedContent(int clock, LocalTime time) throws InterruptedException {
        if (aggregatedContent.size() == 0) {
            return null;
        }

        // Use Tree map to sorted the order of the content and retrived all the content
        // if it happens before this GET request
        StringBuilder allContent = new StringBuilder();
        TreeMap<Integer, String> retrievedAggregatedContent = new TreeMap<Integer, String>();
        for (int key : aggregatedContent.keySet()) {
            if (key < clock) {
                String c = aggregatedContent.get(key).getContent();
                retrievedAggregatedContent.put(key, c);
            }
        }
        int i = 0;
        int start = 0;

        // Obtain the most recent 20
        if (retrievedAggregatedContent.size() > 20) {
            start = retrievedAggregatedContent.size() - 20;
        }

        // Check if the content is alive
        for (Entry<Integer, String> entry : retrievedAggregatedContent.entrySet()) {
            if (i >= start) {
                LocalTime t = lastSeen.get(entry.getKey());
                if (time.isBefore(t.plusSeconds(12)))
                    allContent.append(entry.getValue());
            }
            i++;
        }

        // No Content return null
        if (allContent.length() == 0) {
            return null;
        }
        return allContent.toString();
    }

    // Remove without concurrent issue
    public void removeContent(String serverID, int clock) {
        Iterator<Entry<Integer, Content>> iterator = aggregatedContent.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Content> entry = iterator.next();
            if (entry.getKey().equals(clock)) {
                iterator.remove();
            }
        }
    }

    public StringBuilder toSting() {
        StringBuilder tmp = new StringBuilder();
        for (int key : aggregatedContent.keySet()) {
            // System.out.println(String.format("%d : %s", key,
            // aggregatedContent.get(key).getContent()));
            tmp.append(aggregatedContent.get(key).getContent());
        }
        return tmp;
    }

    public void setLastSeen(String serverID, LocalTime time) {
        if (serverPool.containsKey(serverID)) {
            for (int clock : serverPool.get(serverID)) {
                lastSeen.put(clock, time);
            }
        }
    }

    private void replicate(String fileName, Object object) throws Exception {
        try {
            File file = new File(fileName);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(object);
            s.close();
        } catch (Exception e) {
        }
    }
}