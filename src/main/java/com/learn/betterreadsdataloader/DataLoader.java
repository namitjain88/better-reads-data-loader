package com.learn.betterreadsdataloader;

import com.learn.betterreadsdataloader.author.Author;
import com.learn.betterreadsdataloader.author.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DataLoader {

    @Autowired
    private AuthorRepository authorRepository;

    @PostConstruct
    public void loadData() {
        Author author = new Author();
        author.setId("id");
        author.setName("name");
        author.setPersonalName("personal name");

        authorRepository.save(author);
    }
}
