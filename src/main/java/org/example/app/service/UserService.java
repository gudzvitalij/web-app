package org.example.app.service;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.example.app.domain.Anonymous;
import org.example.app.domain.Code;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.dto.*;
import org.example.app.exception.PasswordNotMatchesException;
import org.example.app.exception.RegistrationException;
import org.example.app.exception.UserNotFoundException;
import org.example.app.jpa.JpaTransactionTemplate;
import org.example.app.repository.UserRepository;
import org.example.framework.security.*;
import org.example.framework.util.KeyValue;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserService implements AuthenticationProvider, AnonymousProvider {
  private final UserRepository repository;
  private final JpaTransactionTemplate transactionTemplate;
  private final PasswordEncoder passwordEncoder;
  private final StringKeyGenerator keyGenerator;

  @Override
  public Authentication authenticate(Authentication authentication) {
    final var token = (String) authentication.getPrincipal();

    return repository.findByToken(token)
        .map(o -> new TokenAuthentication(o, null, List.of(), true))
        .orElseThrow(AuthenticationException::new);
  }

  @Override
  public AnonymousAuthentication provide() {
    // "ROLE_ANONYMOUS"
    return new AnonymousAuthentication(new Anonymous());
  }

  public RegistrationResponseDto register(RegistrationRequestDto requestDto) {
    // TODO login:
    //  case-sensitivity: coursar Coursar
    //  cleaning: "  Coursar   "
    //  allowed symbols: [A-Za-z0-9]{2,60}
    //  mis...: Admin, Support, root, ...
    //  мат: ...
    // FIXME: check for nullability
    final var username = requestDto.getUsername().trim().toLowerCase();
    // TODO password:
    //  min-length: 8
    //  max-length: 64
    //  non-dictionary
    final var password = requestDto.getPassword().trim();
    final var hash = passwordEncoder.encode(password);
    final var token = keyGenerator.generateKey();
    final var saved = repository.save(0, username, hash).orElseThrow(RegistrationException::new);

    repository.saveToken(saved.getId(), token);
    // "ROLE_USER"
    repository.saveRoleUser(saved.getId(), Roles.ROLE_USER);
    return new RegistrationResponseDto(saved.getId(), saved.getUsername(), token);
  }

  public LoginResponseDto login(LoginRequestDto requestDto) {
    final var username = requestDto.getUsername().trim().toLowerCase();
    final var password = requestDto.getPassword().trim();

    final var result = transactionTemplate.executeInTransaction((entityManager, transaction) -> {
      final var saved = repository.getByUsernameWithPassword(
          entityManager,
          transaction,
          username
      ).orElseThrow(UserNotFoundException::new);

      // TODO: be careful - slow
      if (!passwordEncoder.matches(password, saved.getPassword())) {
        // FIXME: Security issue
        throw new PasswordNotMatchesException();
      }

      final var token = keyGenerator.generateKey();
      repository.saveToken(saved.getId(), token);
      return new KeyValue<>(token, saved);
    });

    // FIXME: Security issue

    final var token = result.getKey();
    final var saved = result.getValue();
    return new LoginResponseDto(saved.getId(), saved.getUsername(), token);
  }

  public String generateCode(CodeRequestDto codeRequestDto) {
    final var username = codeRequestDto.getUsername();

    if (username.isEmpty()) {
      throw new UserNotFoundException();
    }

    User user = repository.getByUsername(username).orElseThrow(UserNotFoundException::new);
    SecureRandom secureRandom = new SecureRandom();
    final int number = secureRandom.nextInt(999999);
    final var code = String.format("%06d", number);
    repository.saveCode(code, user.getId());
    return code;
  }

  public CodeResponseDto restoreCredentials(CodeRequestDto codeRequestDto) {
    final var response = new CodeResponseDto();

    if (!codeRequestDto.getRestoreCode().isEmpty()  && !codeRequestDto.getUsername().isEmpty()  && !codeRequestDto.getNewCode().isEmpty()) {
      changeCredentials(codeRequestDto);
      response.setMessage("code changed");
    } else {
      generateCode(codeRequestDto);
      response.setMessage("code generated");
    }
    return response;
  }

  public void changeCredentials(CodeRequestDto codeRequestDto) {
    User user = repository.getByUsername(codeRequestDto.getUsername()).orElseThrow(UserNotFoundException::new);
    Code code = repository.getCodeById(user.getId()).orElseThrow(RuntimeException::new);

    String inCode = codeRequestDto.getRestoreCode();
    String outCode = code.getCode();

    if (inCode.equals(outCode)) {
      final var password = codeRequestDto.getNewCode().trim();
      final var hash = passwordEncoder.encode(password);
      repository.save(user.getId(), user.getUsername(), hash).orElseThrow(RegistrationException::new);
    }
  }




}
