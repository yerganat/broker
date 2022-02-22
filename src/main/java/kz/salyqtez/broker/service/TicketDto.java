package kz.salyqtez.broker.service;

import java.util.Date;

public class TicketDto {
    private String ticker;
    private boolean isSell;
    private Double price;
    private Double count;
    private Date timestamp;
    private Double rate;


    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public boolean isSell() {
        return isSell;
    }

    public boolean isBuy() {
        return !isSell;
    }

    public void setSell(boolean sell) {
        this.isSell = sell;
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

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
