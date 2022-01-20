package kz.salyqtez.broker.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(nullable = false)
    private Double rate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}

