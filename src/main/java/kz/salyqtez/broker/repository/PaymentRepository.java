package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
