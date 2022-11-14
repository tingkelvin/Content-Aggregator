package xml;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RSSFeed {
    private RSSHeader header;
    private RSSAuthor author;
    private List<RSSEntry> entries;

    public RSSFeed() {
    };

    public RSSFeed(RSSHeader header, RSSAuthor author, List<RSSEntry> entries) {
        this.header = header;
        this.author = author;
        this.entries = entries;
    }

    public void setHeader(RSSHeader header) {
        this.header = header;
    }

    public RSSAuthor getAuthor() {
        return author;
    }

    public void setAuthor(RSSAuthor author) {
        this.author = author;
    }

    public void setEntries(List<RSSEntry> entries) {
        this.entries = entries;
    }

    public RSSHeader getHeader() {
        return header;
    }

    public List<RSSEntry> getEntries() {
        return entries;
    }

    public static String formatDate(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        return sdf.format(cal.getTime());
    }
}