package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.RegisterUserUseCase;
import app.jobzy.api.application.port.in.command.RegisterUserCommand;
import app.jobzy.api.application.port.out.CompanyRepository;
import app.jobzy.api.application.port.out.PublicEmailDomains;
import app.jobzy.api.application.port.out.RegistrationMailer;
import app.jobzy.api.application.port.out.UserRepository;
import app.jobzy.api.domain.identity.Company;
import app.jobzy.api.domain.identity.InvalidEmailAddressException;
import app.jobzy.api.domain.identity.User;
import app.jobzy.api.domain.identity.valueobject.EmailAddress;
import app.jobzy.api.domain.identity.valueobject.Registrant;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers a user with a work e-mail address (ADR 0003). The e-mail domain decides the company: an
 * unknown domain founds a new company with the user as owner, a known one makes the user a member
 * of it.
 *
 * <p>Whether the address is already registered never shows in the outcome, only in which mail is
 * sent: an unconfirmed registration gets a fresh verification link, a confirmed account a mail that
 * the account exists. The details of a repeated registration (name, job role, company name) are
 * ignored; the first registration stands.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {
  private final UserRepository userRepository;
  private final CompanyRepository companyRepository;
  private final PublicEmailDomains publicEmailDomains;
  private final VerificationTokenIssuer verificationTokenIssuer;
  private final RegistrationMailer registrationMailer;
  private final Clock clock;

  @Override
  @Transactional
  public void register(RegisterUserCommand command) {
    var email = new EmailAddress(command.email());
    if (publicEmailDomains.isPublic(email.domain())) {
      throw new InvalidEmailAddressException(
          "Use your work e-mail address; public mail domains are not accepted");
    }
    var now = LocalDateTime.now(clock);

    userRepository
        .findByEmail(email)
        .ifPresentOrElse(
            existingUser -> registerAgain(existingUser, now),
            () -> registerNewUser(command, email, now));
  }

  private void registerNewUser(RegisterUserCommand command, EmailAddress email, LocalDateTime now) {
    Optional<Company> existingCompany = companyRepository.findByEmailDomain(email.domain());
    Company company =
        existingCompany.orElseGet(() -> foundCompany(command.companyName(), email.domain(), now));

    var registrant =
        new Registrant(command.firstName(), command.lastName(), email, command.jobRole());
    var issuedToken = verificationTokenIssuer.issue();
    var user =
        User.register(
            registrant, company.getId(), existingCompany.isPresent(), issuedToken.token(), now);
    userRepository.save(user);

    log.info("User is registered: {}", user);
    registrationMailer.sendVerificationMail(user, issuedToken.rawToken());
  }

  private Company foundCompany(String companyName, String emailDomain, LocalDateTime now) {
    var company = Company.found(companyName, emailDomain, now);
    companyRepository.save(company);
    log.info("Company is founded: {}", company);
    return company;
  }

  private void registerAgain(User existingUser, LocalDateTime now) {
    if (!existingUser.isAwaitingVerification()) {
      log.info("Registration for an existing account: {}", existingUser);
      registrationMailer.sendAccountExistsMail(existingUser);
      return;
    }
    var issuedToken = verificationTokenIssuer.issue();
    existingUser.renewVerificationToken(issuedToken.token(), now);
    userRepository.save(existingUser);

    log.info("Verification token is renewed: {}", existingUser);
    registrationMailer.sendVerificationMail(existingUser, issuedToken.rawToken());
  }
}
