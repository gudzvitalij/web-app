package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.TransferRequestDto;
import org.example.app.dto.TransferResponseDto;
import org.example.app.exception.CardNotFoundException;
import org.example.app.repository.CardRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CardService {
  private final CardRepository cardRepository;

  public List<Card> getAllByOwnerId(long ownerId) {
    return cardRepository.getAllByOwnerId(ownerId);
  }

  public List<Card> getAll(){
    return cardRepository.getAll();
  }

  public Optional<Card> getCardById(long cardId){
    return cardRepository.getCardById(cardId);
  }

  public int blockCardById(long cardId){
    return cardRepository.blockCardById(cardId);
  }

  public Optional<Card> order(long id, long ownerId, String number, long balance, boolean active) {
    return cardRepository.order(id,ownerId,number,balance,active);
  }

  public TransferResponseDto transfer(long senderCardId, User user, TransferRequestDto requestDto) throws CardNotFoundException {
    final var card = cardRepository.getCardById(senderCardId).get();
    final var recipientCardId = requestDto.getRecipientCardId();
    final var amount = requestDto.getAmount();

    if (user.getId() != cardRepository.getOwnerId(senderCardId).orElseThrow(RuntimeException::new)) {
      throw new RuntimeException();
    }

    if ((card.getBalance() < amount) || (amount < 0)) {
      throw new RuntimeException();
    }

    if (cardRepository.getCardById(recipientCardId).isEmpty()) {
      throw new CardNotFoundException();
    }

    cardRepository.transaction(senderCardId, recipientCardId, amount);
    final var result = cardRepository.getCardById(recipientCardId).orElseThrow(RuntimeException::new);

    return new TransferResponseDto(result.getId(), result.getNumber(), result.getBalance());
  }
}
