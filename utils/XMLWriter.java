package utils;

import java.util.ArrayList;
import java.util.HashMap;

import xml.RSSAuthor;
import xml.RSSEntry;
import xml.RSSFeed;
import xml.RSSHeader;
import xml.RSSWriter;

import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner; // Import the Scanner class to read text files

public class XMLWriter {
    static private String write(String textFile, String contentDir) throws Exception {
        String fileName = textFile.substring(0, textFile.indexOf('.'));
        String curDir = System.getProperty("user.dir") + "/";
        HashMap<String, String> elements = new HashMap<String, String>();
        ArrayList<RSSEntry> entries = new ArrayList<RSSEntry>();
        RSSHeader header;
        RSSAuthor author;
        RSSFeed feed = new RSSFeed();
        try {
            File myObj = new File(curDir + contentDir + textFile);
            Scanner myReader = new Scanner(myObj);
            elements.put("email", "");
            boolean loadingEntires = false;
            RSSEntry entry = null;
            if(!myReader.hasNextLine()){
                return null;
            }
            while (myReader.hasNextLine()) {
                String str = myReader.nextLine();

                String ele;
                if (!str.equals("entry")) {
                    ele = str.substring(0, str.indexOf(':'));
                    elements.put(ele, str.substring(str.indexOf(':')));
                } else {
                    if (loadingEntires) {
                       
                        entry = new RSSEntry(elements.get("title"), elements.get("link"),
                                elements.get("id"), elements.get("updated"),
                                elements.get("summary"));
                        //  System.out.println("line: -----------" + entry.getTitle());
                        entries.add(entry);

                    } else {
                        header = new RSSHeader(elements.get("title"), elements.get("subtitle"),
                                elements.get("link"), elements.get("updated"),
                                elements.get("id"));
                        author = new RSSAuthor(elements.get("author"), elements.get("email"));
                        loadingEntires = true;
                        feed.setHeader(header);
                        feed.setAuthor(author);
                    }
                }
                if(!myReader.hasNextLine()){
                    entry = new RSSEntry(elements.get("title"), elements.get("link"),
                            elements.get("id"), elements.get("updated"),
                            elements.get("summary"));
                    //  System.out.println("line: -----------" + entry.getTitle());
                    entries.add(entry);
                }

            }
            feed.setEntries(entries);
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        String s = RSSWriter.write(feed, contentDir + fileName + ".xml");
        // System.out.println("line: " + s);
        return s;

    }

    static public String convert(String textFile) {
        String xmlContent = new String();
        try {
            return write(textFile, "contents/");
            // xmlContent = Files.readString(Path.of(xmlFile));
            // Runtime.getRuntime().exec("rm " + xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlContent;
    }

    public static void main(String[] args) {

    }
}