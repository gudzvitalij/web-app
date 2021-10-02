package org.example.app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.service.CardService;
import org.example.app.service.UserService;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.Authentication;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class UserHelper {
  private UserHelper() {
  }

  // TODO: beautify
  public static User getUser(HttpServletRequest req) {
    return ((User) ((Authentication) req.getAttribute(RequestAttributes.AUTH_ATTR)).getPrincipal());
  }

  public static Collection<String> getUserRoles(HttpServletRequest req) {
    return Stream.of(req.getAttribute(RequestAttributes.AUTH_ATTR))
            .map(o -> ((Authentication)o).getAuthorities())
            .findAny()
            .orElseThrow(RuntimeException::new);
  }

  public static Collection<String> getRole(HttpServletRequest req) {
    Authentication auth = (Authentication) req.getAttribute(RequestAttributes.AUTH_ATTR);
    return (Collection<String>) auth.getCredentials();
  }



}
