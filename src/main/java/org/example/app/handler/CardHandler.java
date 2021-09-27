package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.TransferRequestDto;
import org.example.app.exception.CardNotFoundException;
import org.example.app.service.CardService;
import org.example.app.util.UserHelper;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.Authentication;
import org.example.framework.security.Roles;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;

@Log
@RequiredArgsConstructor
public class CardHandler { // Servlet -> Controller -> Service (domain) -> domain
  private final CardService service;
  private final Gson gson;

  public void getAll(HttpServletRequest req, HttpServletResponse resp) {
    try {
      // cards.getAll?ownerId=1
      final var user = UserHelper.getUser(req);
      final List<Card> data;

      if (((List<?>)((Authentication)req.
              getAttribute(RequestAttributes.AUTH_ATTR)).
              getCredentials()).
              contains(Roles.ROLE_ADMIN)){
        data = service.getAll();
      }
      else {
        data = service.getAllByOwnerId(user.getId());
      }


      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(data));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void getById(HttpServletRequest req, HttpServletResponse resp) {
    final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR)).group("cardId"));
    log.log(Level.INFO, "getById");
  }



  public void order(HttpServletRequest req, HttpServletResponse resp) {
    final var cardId = Long.parseLong(((Matcher) req
            .getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
            .group("cardId"));
    final var roles = UserHelper.getUserRoles(req);
    if (roles.contains(Roles.ROLE_ADMIN) || userIsOwner(req)){
      log.log(Level.INFO, "ordered");
    }
  }


  public void blockById(HttpServletRequest req, HttpServletResponse resp) {
    try {
      final var cardId = Long.parseLong(((Matcher) req
              .getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
              .group("cardId"));
      final var roles = UserHelper.getUserRoles(req);
      if (roles.contains(Roles.ROLE_ADMIN) || userIsOwner(req)) {
        final var data = service.blockCardById(cardId);
        resp.setHeader("Content-Type", "application/json");
        resp.getWriter().write(gson.toJson(data));
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public boolean userIsOwner(HttpServletRequest req){
    final var cardId = Long.parseLong(((Matcher) req
            .getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
            .group("cardId"));
    final var user = UserHelper.getUser(req);
    final var ownerId = service.getCardById(cardId)
            .map(Card::getOwnerId)
            .orElseThrow(RuntimeException::new);
    return (user.getId() == ownerId);
  }

  public void transaction(HttpServletRequest req, HttpServletResponse resp) {
    try {
      final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
              .group("cardId"));
      final var user = UserHelper.getUser(req);
      final var requestDto = gson.fromJson(req.getReader(), TransferRequestDto.class);
      final var responseDto = service.transfer(cardId, user, requestDto);
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(responseDto));
    } catch (IOException | CardNotFoundException e) {
      e.printStackTrace();
    }
  }

}
