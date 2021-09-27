package org.example.app.domain;

public class Card {
  private long id;
  private String number;
  private long balance;
  private long ownerId;

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  public Card(long id, String number, long balance, long ownerId) {
    this.id = id;
    this.number = number;
    this.balance = balance;
    this.ownerId = ownerId;
  }

  public Card(long id, String number, long balance) {
    this.id = id;
    this.number = number;
    this.balance = balance;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public long getBalance() {
    return balance;
  }

  public void setBalance(long balance) {
    this.balance = balance;
  }


}
