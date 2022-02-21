package kz.salyqtez.broker.service;

import kz.salyqtez.broker.model.Payment;
import kz.salyqtez.broker.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class PayboxService {
    private final PaymentRepository paymentRepository;

    public PayboxService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void savePayment(Long botUserId, List<TicketDto> tickers) {
        Payment payment = new Payment();
        payment.setBotUserId(botUserId);
        payment.setTickers(tickers);
        payment.setAmount(100L);
        payment.setPayed(false);
        payment.setTimestamp(new Date());
        paymentRepository.save(payment);
    }
}
