package xml;

public class RSSHeader {
  private String title = "";
  private String subtitle = "";
  private String link = "";
  private String updated = "";
  private String ID = "";

  public RSSHeader(String title, String subtitle, String link, String updated, String ID) {
    this.title = title;
    this.subtitle = subtitle;
    this.link = link;
    this.updated = updated;
    this.ID = ID;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void getSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getUpdated() {
    return updated;
  }

  public void setUpdate(String updated) {
    this.updated = updated;
  }

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }
}