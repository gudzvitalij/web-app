package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Code;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.entity.UserEntity;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepository {
  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<User> rowMapper = resultSet -> new User(
      resultSet.getLong("id"),
      resultSet.getString("username")
  );
  private final RowMapper<UserWithPassword> rowMapperWithPassword = resultSet -> new UserWithPassword(
      resultSet.getLong("id"),
      resultSet.getString("username"),
      resultSet.getString("password")
  );

  private final RowMapper<String> rowMapperRole = resultSet -> resultSet.getString("role");

  private final RowMapper<Code> rowMapperCode = resultSet -> new Code(
          resultSet.getString("code"),
          resultSet.getLong("userId")
  );


  public Optional<User> getByUsername(String username) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne("SELECT id, username FROM users WHERE username = ?", rowMapper, username);
  }

  public Optional<UserWithPassword> getByUsernameWithPassword(EntityManager entityManager, EntityTransaction transaction, String username) {
    // em, emt - closeable
    return entityManager.createNamedQuery(UserEntity.FIND_BY_USERNAME, UserEntity.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultStream()
        .map(o -> new UserWithPassword(o.getId(), o.getUsername(), o.getPassword()))
        .findFirst();
    // language=PostgreSQL
    // return jdbcTemplate.queryOne("SELECT id, username, password FROM users WHERE username = ?", rowMapperWithPassword, username);
  }

  /**
   * saves user to db
   *
   * @param id       - user id, if 0 - insert, if not 0 - update
   * @param username
   * @param hash
   */
  // TODO: DuplicateKeyException <-
  public Optional<User> save(long id, String username, String hash) {
    // language=PostgreSQL
    return id == 0 ? jdbcTemplate.queryOne(
        """
            INSERT INTO users(username, password) VALUES (?, ?) RETURNING id, username
            """,
        rowMapper,
        username, hash
    ) : jdbcTemplate.queryOne(
        """
            UPDATE users SET username = ?, password = ? WHERE id = ? RETURNING id, username
            """,
        rowMapper,
        username, hash, id
    );
  }

  public Optional<User> findByToken(String token) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
        """
            SELECT u.id, u.username FROM tokens t
            JOIN users u ON t."userId" = u.id
            WHERE t.token = ?
            """,
        rowMapper,
        token
    );
  }

  public void saveToken(long userId, String token) {
    // query - SELECT'???? (ResultSet)
    // update - ? int/long
    // language=PostgreSQL
    jdbcTemplate.update(
        """
            INSERT INTO tokens(token, "userId") VALUES (?, ?)
            """,
        token, userId
    );
  }

  public void saveRoleUser(long userId, String role){
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                    INSERT INTO roles(role, "userId") VALUES (?, ?)
                    """,
            role, userId
    );
  }

  public Optional<User> getByUserId(long id) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
            """
                SELECT id, username FROM users  WHERE id = ?
                """,
            rowMapper,
            id
    );
  }

  public List<String> getRoleByUserId(long userId) {
    // language=PostgreSQL
    return jdbcTemplate.queryAll(
            """
              SELECT role, "userId" FROM roles WHERE "userId" = ?
              """,
            rowMapperRole,
            userId
    );
  }

  public void saveCode(String code, long userId) {
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                INSERT INTO reset_codes(code, "userId") VALUES (?, ?)
                """,
            code, userId
    );
  }

  public Optional<Code> getCodeById(long userId) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
            """
                SELECT "userId", code FROM reset_codes
                WHERE "userId" = ?
                AND active = true
                ORDER BY created DESC
                LIMIT 1
                """,
            rowMapperCode,
            userId
    );
  }

}
