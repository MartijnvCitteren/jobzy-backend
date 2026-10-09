package app.jobzy.api.adapter.out.persistence.identity.mapper;

import app.jobzy.api.adapter.out.persistence.identity.UserCredentialsJpaEntity;
import app.jobzy.api.adapter.out.persistence.identity.UserJpaEntity;
import app.jobzy.api.domain.identity.User;
import app.jobzy.api.domain.identity.valueobject.EmailAddress;
import app.jobzy.api.domain.identity.valueobject.UserCredentials;
import app.jobzy.api.domain.identity.valueobject.VerificationToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Maps between the {@code User} aggregate and {@code UserJpaEntity}. The verification token is
 * flattened into two nullable columns; a user without a token hash has no token.
 */
@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UserJpaMapper {

  /**
   * Maps the user without its credentials: the credentials row shares the user's primary key and is
   * wired by {@code UserRepositoryAdapter}.
   */
  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "verificationTokenHash", source = "verificationToken.tokenHash")
  @Mapping(target = "verificationTokenExpiresAt", source = "verificationToken.expiresAt")
  @Mapping(target = "credentials", ignore = true)
  UserJpaEntity toJpaEntity(User user);

  @Mapping(target = "verificationToken", source = ".")
  User toDomain(UserJpaEntity entity);

  default EmailAddress toEmailAddress(String email) {
    return email == null ? null : new EmailAddress(email);
  }

  default VerificationToken toVerificationToken(UserJpaEntity entity) {
    if (entity.getVerificationTokenHash() == null) {
      return null;
    }
    return new VerificationToken(
        entity.getVerificationTokenHash(), entity.getVerificationTokenExpiresAt());
  }

  default UserCredentials toCredentials(UserCredentialsJpaEntity entity) {
    return entity == null ? null : new UserCredentials(entity.getPasswordHash());
  }
}
