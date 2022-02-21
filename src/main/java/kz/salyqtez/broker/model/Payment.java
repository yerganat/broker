package kz.salyqtez.broker.model;

import kz.salyqtez.broker.service.TicketConverter;
import kz.salyqtez.broker.service.TicketDto;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "payment")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column
  private Long botUserId;

  @Column
  private Long payboxTxId;

  @Column
  private Long amount;

  @Column
  private boolean isPayed;

  @Column(nullable = false)
  @CreationTimestamp
  private Date timestamp;

  @Column
  @Convert(converter = TicketConverter.class)
  private List<TicketDto> tickers;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Long getBotUserId() {
    return botUserId;
  }

  public void setBotUserId(Long botUserId) {
    this.botUserId = botUserId;
  }

  public Long getPayboxTxId() {
    return payboxTxId;
  }

  public void setPayboxTxId(Long payboxTxId) {
    this.payboxTxId = payboxTxId;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public boolean isPayed() {
    return isPayed;
  }

  public void setPayed(boolean payed) {
    isPayed = payed;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public List<TicketDto> getTickers() {
    return tickers;
  }

  public void setTickers(List<TicketDto> tickers) {
    this.tickers = tickers;
  }
}

