package org.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Card {
  private long id;
  private String number;
  private long balance;
  private long ownerId;


}
