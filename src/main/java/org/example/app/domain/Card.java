package org.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Card {
  private long id;
  private String number;
  private long balance;
  private long ownerId;

  public Card(long id, String number, long balance) {
    this.id = id;
    this.number = number;
    this.balance = balance;
  }

}
