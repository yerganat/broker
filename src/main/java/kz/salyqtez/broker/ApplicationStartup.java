package kz.salyqtez.broker;

import kz.salyqtez.broker.model.Exchange;
import kz.salyqtez.broker.repository.ExchangeRepository;
import kz.salyqtez.broker.service.RateCache;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final ExchangeRepository rateRepository;

    public ApplicationStartup(ExchangeRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        List<Exchange> rateList = rateRepository.findAll();

        for (Exchange rate:rateList) {
            RateCache.val.put(rate.getDate().getTime(), rate.getRate());
        }

        System.out.println("=====  RATE cache is executed! count is: " + RateCache.val.size());
    }
}