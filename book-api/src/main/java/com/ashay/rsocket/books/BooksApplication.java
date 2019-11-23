package com.ashay.rsocket.books;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.micrometer.MicrometerDuplexConnectionInterceptor;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
interface BookRepo extends ReactiveMongoRepository<Book, Long> {

    public Mono<Book> findBookByName(String bookName);

}

@Configuration
class RSocketConfig {

    @Bean
    public ServerRSocketFactoryProcessor serverRSocketFactoryProcessor(MeterRegistry meterRegistry) {
        return factory -> factory.addResponderPlugin(new MicrometerRSocketInterceptor(meterRegistry))
                .addConnectionPlugin(new MicrometerDuplexConnectionInterceptor(meterRegistry));
    }
}

@SpringBootApplication
public class BooksApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooksApplication.class, args);
    }

}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Book {
    @MongoId
    String id;
    String name;
    String author;
    String isbn;
    double price;
}

@Controller
@Slf4j
class Books {

    private final BookRepo bookRepo;

    Books(BookRepo bookRepo) {
        this.bookRepo = bookRepo;
    }

    @MessageMapping("/all-books")
    public Flux<Book> getAll() {
        return bookRepo.findAll()
                .doOnNext(book -> log.info("sent {}", book.id));
    }

    @MessageMapping("/book")
    public Mono<Book> getBook() {
        return bookRepo.findOne(Example.of(Book.builder()
                .name("name_10")
                .build()));
    }

    @PostMapping("/seed")
    @ResponseBody
    public Mono<Void> populateSeed(@RequestParam("count") Integer count) {
        return Flux.range(0, count)
                .flatMap(integer -> {
                    Book book = Book.builder()
                            .author("Author_" + integer)
                            .isbn("isbn_" + integer)
                            .name("name_" + integer)
                            .price(integer)
                            .build();
                    return bookRepo.save(book);
                })
                .doOnNext(book -> log.info("Saved {}", book.id))
                .then();
    }

    @GetMapping("/count")
    @ResponseBody
    public Mono<Long> count() {
        return bookRepo.count();
    }
}
