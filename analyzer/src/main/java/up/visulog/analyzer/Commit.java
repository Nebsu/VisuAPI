package up.visulog.analyzer;

public class Commit {
    
    protected final String title;
    protected final String date;
    protected final String author;

    public Commit(String title, String date, String author) {
        this.title = title;
        this.date = date;
        this.author = author;
    }

    @Override
    public String toString() {
        return (this.date + " | " + this.author + "\n: " + this.title);
    }

}