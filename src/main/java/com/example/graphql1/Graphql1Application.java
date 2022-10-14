package com.example.graphql1;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
public class Graphql1Application {
    public static void main(String[] args) {
        SpringApplication.run( com.example.graphql1.Graphql1Application.class, args);
    }

}


@Controller
class BookController {

    private final com.example.graphql1.BookRepository bookRepository;

    public BookController(com.example.graphql1.BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @QueryMapping
    public List<com.example.graphql1.Book> allBooks() {
        return bookRepository.findAll();
    }

    @QueryMapping
    public com.example.graphql1.Book findOne(@Argument Integer id) {
        com.example.graphql1.Book book = bookRepository.findById(id).orElse(null);
        return book;
    }
}


@Controller
class AuthorController {

    private final com.example.graphql1.AuthorRepository authorRepository;
    private final com.example.graphql1.BookRepository bookRepository;

    public AuthorController(com.example.graphql1.AuthorRepository authorRepository, com.example.graphql1.BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @QueryMapping
    public List<com.example.graphql1.Author> allAuthors() {
        List<com.example.graphql1.Author> authors = authorRepository.findAll();
        for (com.example.graphql1.Author author : authors) {
            author.setBooks(bookRepository.findAllByAuthor_Id(author.getId()));
        }
        return authors;
    }

    //@SchemaMapping(typeName = "Mutation", value = "updateAuthor")
    @MutationMapping
    public com.example.graphql1.Author updateAuthor(@Argument com.example.graphql1.AuthorUpdateDTO dto) {
        com.example.graphql1.Author author = authorRepository.findById(dto.getId()).orElseThrow(() -> {
            throw new RuntimeException("Not  found");
        });
        if (Objects.nonNull(dto.getFirstName()))
            author.setFirstName(dto.getFirstName());
        if (Objects.nonNull(dto.getLastName()))
            author.setLastName(dto.getLastName());
        authorRepository.save(author);
        return author;
    }

}

interface BookRepository extends JpaRepository<com.example.graphql1.Book, Integer> {
    List<com.example.graphql1.Book> findAllByAuthor_Id(Integer id);
}


interface AuthorRepository extends JpaRepository<com.example.graphql1.Author, Integer> {
}


@Component
class Init implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        com.example.graphql1.AuthorRepository authorRepository = applicationContext.getBean( com.example.graphql1.AuthorRepository.class);
        com.example.graphql1.BookRepository bookRepository = applicationContext.getBean( com.example.graphql1.BookRepository.class);

        authorRepository.deleteAll();
        bookRepository.deleteAll();

        com.example.graphql1.Author josh = new com.example.graphql1.Author (1, "Josh", "Long");
        com.example.graphql1.Author mark = new com.example.graphql1.Author (2, "Mark", "Heckler");
        com.example.graphql1.Author greg = new com.example.graphql1.Author (3, "Greg", "Turnquist");
        authorRepository.saveAllAndFlush(Arrays.asList(josh, mark, greg));

        bookRepository.saveAll(
                List.of(
                        new com.example.graphql1.Book (1, "Reactive Spring", 484, com.example.graphql1.Rating.FIVE_STARS, josh),
                        new com.example.graphql1.Book (2, "Spring Boot Up & Running", 328, com.example.graphql1.Rating.FIVE_STARS, mark),
                        new com.example.graphql1.Book (3, "Hacking with Spring Boot 2.3", 392, com.example.graphql1.Rating.FIVE_STARS, greg)
                )
        );

    }
}

enum Rating {
    FIVE_STARS("⭐️⭐️⭐️⭐️⭐️️️️", "5"),
    FOUR_STARS("⭐️⭐️⭐️⭐️", "4"),
    THREE_STARS("⭐️⭐️⭐️", "3"),
    TWO_STARS("⭐️⭐️", "2"),
    ONE_STAR("⭐️", "1");

    private String star;
    private String rating;

    Rating(String star, String rating) {
        this.star = star;
        this.rating = rating;
    }

    @JsonValue
    public String getStar() {
        return star;
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
class Book {
    @Id
    private Integer id;
    private String title;
    private Integer pages;
    private com.example.graphql1.Rating rating;

    @ManyToOne(targetEntity = com.example.graphql1.Author.class, fetch = FetchType.LAZY)
    private com.example.graphql1.Author author;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
class Product {
    @Id
    private Integer id;
    private String name;
    private String description;
    private com.example.graphql1.Size size;
}

interface ProductRepo extends JpaRepository<com.example.graphql1.Product, Integer> {
}

enum Size {
    S, M, L, XL, XXL
}


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_auhor")
class Author {
    @Id
    private Integer id;
    private String firstName;
    private String lastName;

    @OneToMany
    private List<com.example.graphql1.Book> books;

    public Author(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    public String fullName() {
        return firstName + " " + lastName;
    }

}


@Data
@NoArgsConstructor
@AllArgsConstructor
class AuthorUpdateDTO {
    private Integer id;
    private String firstName;
    private String lastName;
}