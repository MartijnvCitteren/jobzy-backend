package app.jobzy.agent;

public interface Agent<T, R> {
  R execute(T prompt);
}
