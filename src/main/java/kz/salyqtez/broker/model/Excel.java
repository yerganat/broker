package kz.salyqtez.broker.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "excel")
public class Excel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(nullable = false)
  private String user;

  @Column
  private String name;

  @Column
  private String botFileId;

  @Column
  private String botUserId;

  @Column
  private String description;

  @Column
  private boolean processed;

//  @Column(nullable = false, unique = true) TODO
  @Column(nullable = false)
  private String hash;

  @Column(nullable = false)
  private long bytes;

  @Column(nullable = false)
  @CreationTimestamp
  private Date timestamp;

  public Excel() {

  }

  public Excel(String name, String description, boolean processed) {
    this.name = name;
    this.description = description;
    this.processed = processed;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public long getBytes() {
    return bytes;
  }

  public void setBytes(long bytes) {
    this.bytes = bytes;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getBotFileId() {
    return botFileId;
  }

  public void setBotFileId(String botFileId) {
    this.botFileId = botFileId;
  }

  public String getBotUserId() {
    return botUserId;
  }

  public void setBotUserId(String botUserId) {
    this.botUserId = botUserId;
  }
}

