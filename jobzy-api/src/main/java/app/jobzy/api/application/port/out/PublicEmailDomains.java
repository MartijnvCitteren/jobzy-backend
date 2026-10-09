package app.jobzy.api.application.port.out;

/**
 * The block list of public mail domains (gmail, outlook, proton, ...). An address on such a domain
 * says nothing about the company someone works for, so it cannot be used to register.
 */
public interface PublicEmailDomains {

  /**
   * Whether the domain is a public mail domain.
   *
   * @param domain an e-mail domain in lower case
   */
  boolean isPublic(String domain);
}
