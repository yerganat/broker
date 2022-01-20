package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Exchange;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ExchangeRepository extends CrudRepository<Exchange, Long> {
    List<Exchange> findByDate(Date date);
}