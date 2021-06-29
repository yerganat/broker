package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findByTitle(String title);
}