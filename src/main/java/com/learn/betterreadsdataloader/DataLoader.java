package com.learn.betterreadsdataloader;

import com.learn.betterreadsdataloader.author.Author;
import com.learn.betterreadsdataloader.author.AuthorRepository;
import com.learn.betterreadsdataloader.book.Book;
import com.learn.betterreadsdataloader.book.BookRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataLoader {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Value("${datadump.location.authors}")
    private String authorsDumpLocation;

    @Value("${datadump.location.works}")
    private String worksDumpLocation;

    @PostConstruct
    public void loadData() {
        initAuthors();
        initBooks();
    }

    private void initAuthors() {
        Path path = Paths.get(authorsDumpLocation);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(lineStr -> {
                //stripping the text before { in each line to get actual json object
                String jsonString = lineStr.substring(lineStr.indexOf("{"));
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    Author author = new Author();
                    author.setId(jsonObject.optString("key").replace("/authors/", ""));
                    author.setName(jsonObject.optString("name"));
                    author.setPersonalName(jsonObject.optString("personal_name"));

                    //saving book to database
                    System.out.println("Author Name = " + author.getName());
                    authorRepository.save(author);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBooks() {
        Path path = Paths.get(worksDumpLocation);
        try (Stream<String> lines = Files.lines(path)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            lines.forEach(line -> {
                String jsonString = line.substring(line.indexOf("{"));
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    //construct a book object
                    Book book = new Book();
                    book.setId(jsonObject.getString("key").replace("/works/", ""));
                    book.setTitle(jsonObject.optString("title"));

                    JSONObject descriptionObj = jsonObject.optJSONObject("description");
                    if (descriptionObj != null) {
                        book.setDescription(descriptionObj.optString("value"));
                    }

                    JSONObject publishedObj = jsonObject.optJSONObject("created");
                    if (publishedObj != null) {
                        book.setPublishedDate(LocalDate.parse(publishedObj.optString("value"), dtf));
                    }

                    JSONArray coversJsonArr = jsonObject.optJSONArray("covers");
                    if (coversJsonArr != null) {
                        List<String> coverIds = new ArrayList<>();
                        for (int i = 0; i < coversJsonArr.length(); i++) {
                            coverIds.add(coversJsonArr.optString(i));
                        }
                        book.setCoverIds(coverIds);
                    }

                    JSONArray authorsJsonArr = jsonObject.optJSONArray("authors");
                    if (authorsJsonArr != null) {
                        List<String> authorIds = new ArrayList<>();
                        for (int i = 0; i < authorsJsonArr.length(); i++) {
                            String authorId = authorsJsonArr.getJSONObject(i).getJSONObject("author").getString("key")
                                    .replace("/authors/", "");
                            authorIds.add(authorId);
                        }
                        book.setAuthorIds(authorIds);

                        //fetch name for each author id
                        List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
                                .map(optionalAuthor -> {
                                    if (!optionalAuthor.isPresent()) return "Unknown author";
                                    return optionalAuthor.get().getName();
                                })
                                .collect(Collectors.toList());
                        book.setAuthorNames(authorNames);
                    }

                    //saving book to database
                    System.out.println("Book Title = " + book.getTitle());
                    bookRepository.save(book);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
