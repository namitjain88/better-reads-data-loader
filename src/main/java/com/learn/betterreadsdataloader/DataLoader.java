package com.learn.betterreadsdataloader;

import com.learn.betterreadsdataloader.author.Author;
import com.learn.betterreadsdataloader.author.AuthorRepository;
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
import java.util.stream.Stream;

@Component
public class DataLoader {

    @Autowired
    private AuthorRepository authorRepository;

    @Value("${datadump.location.authors}")
    private String authorsDumpLocation;

    @Value("${datadump.location.works}")
    private String worksDumpLocation;

    @PostConstruct
    public void loadData() {
        initAuthors();
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

                    authorRepository.save(author);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
