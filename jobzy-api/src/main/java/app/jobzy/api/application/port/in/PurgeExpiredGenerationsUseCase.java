package app.jobzy.api.application.port.in;

public interface PurgeExpiredGenerationsUseCase {

  /**
   * Deletes every generation requested longer ago than the retention period.
   *
   * @return the number of deleted generations
   */
  int purgeExpired();
}
