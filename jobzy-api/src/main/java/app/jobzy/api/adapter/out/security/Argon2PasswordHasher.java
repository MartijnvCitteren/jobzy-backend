package app.jobzy.api.adapter.out.security;

import app.jobzy.api.application.port.out.PasswordHasher;
import app.jobzy.api.domain.identity.valueobject.Password;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hashes passwords with Argon2id, using the minimum parameters the OWASP Password Storage Cheat
 * Sheet recommends: 19 MiB of memory, 2 iterations, parallelism 1. The encoded hash carries its
 * parameters and salt, so the parameters can be raised later without breaking existing hashes.
 */
@Component
public class Argon2PasswordHasher implements PasswordHasher {
  private static final int SALT_LENGTH_BYTES = 16;
  private static final int HASH_LENGTH_BYTES = 32;
  private static final int PARALLELISM = 1;
  private static final int MEMORY_KIB = 19 * 1024;
  private static final int ITERATIONS = 2;

  private final Argon2PasswordEncoder encoder =
      new Argon2PasswordEncoder(
          SALT_LENGTH_BYTES, HASH_LENGTH_BYTES, PARALLELISM, MEMORY_KIB, ITERATIONS);

  @Override
  public String hash(Password password) {
    return encoder.encode(password.value());
  }
}
