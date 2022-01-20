package kz.salyqtez.broker.service;

import java.util.Date;

public class TicketDto {
    public String ticker;
    public String type;
    public Double price;
    public Double count;
    public Date timestamp;

    public Calculate calc = new Calculate();

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Calculate getCalc() {
        return calc;
    }

    static class Calculate {
        public Double sumUsd;
        public Double rate;
        public Double sumKzt;
        public Double tax;

        public Double getSumUsd() {
            return sumUsd;
        }

        public void setSumUsd(Double sumUsd) {
            this.sumUsd = sumUsd;
        }

        public Double getRate() {
            return rate;
        }

        public void setRate(Double rate) {
            this.rate = rate;
        }

        public Double getSumKzt() {
            return sumKzt;
        }

        public void setSumKzt(Double sumKzt) {
            this.sumKzt = sumKzt;
        }

        public Double getTax() {
            return tax;
        }

        public void setTax(Double tax) {
            this.tax = tax;
        }
    }
}
