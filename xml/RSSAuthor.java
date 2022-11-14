package xml;

public class RSSAuthor {
    private String author = "";
    private String email = "";

    public RSSAuthor(String author, String email) {
        this.author = author;
        this.email = email;
    }

    RSSAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

}
