package kz.salyqtez.broker.service;

public class ShortDto {
    private String ticker;
    private int rowIdx;
    private Double count;
    private Double price;

    public ShortDto(String ticker, int rowIdx, Double count, Double price){
        this.ticker = ticker;
        this.rowIdx = rowIdx;
        this.count = count;
        this.price = price;
    }

    public String getTicker() {
        return ticker;
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public Double getCount() {
        return count;
    }

    public Double getPrice() {
        return price;
    }
}
