package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    List<Exchange> findByDate(Date date);
}