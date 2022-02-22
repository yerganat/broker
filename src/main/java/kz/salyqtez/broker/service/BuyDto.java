package kz.salyqtez.broker.service;

public class BuyDto {
    private String ticker;
    private int rowIdx;
    private Double count;

    public BuyDto(String ticker, int rowIdx, Double count){
        this.ticker = ticker;
        this.rowIdx = rowIdx;
        this.count = count;
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

    public void subCount(Double count) {
        this.count -= count;
    }
}
