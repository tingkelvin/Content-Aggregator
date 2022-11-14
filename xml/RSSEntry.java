package xml;

public class RSSEntry {
    private String title = "";
    private String link = "";
    private String ID = "";
    private String updated = "";
    private String summary = "";

    public RSSEntry(String title, String link, String ID, String updated, String summary) {
        this.title = title;
        this.link = link;
        this.ID = ID;
        this.updated = updated;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}