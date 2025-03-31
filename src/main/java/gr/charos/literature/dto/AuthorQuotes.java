package gr.charos.literature.dto;

import java.util.ArrayList;
import java.util.List;

public class AuthorQuotes {
    private final String author;
    private List<String> quotes;
    public AuthorQuotes(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }


    public List<String> getQuotes() {
        if (quotes == null) {
            quotes = new ArrayList<>();
        }
        return quotes;
    }
}
