package app.jobzy.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseObjectTest {

  private static class TestObject extends BaseObject {}

  @Test
  @DisplayName("given new object, when constructed then createdAt and lastModifiedAt are set")
  void givenNewObjectWhenConstructedThenCreatedAtAndLastModifiedAtAreSet() {
    var testObject = new TestObject();

    assertNotNull(testObject.getCreatedAt());
    assertEquals(testObject.getCreatedAt(), testObject.getLastModifiedAt());
    assertNull(testObject.getModifiedBy());
  }

  @Test
  @DisplayName("given object, when setLastModifiedAt then getLastModifiedAt returns new value")
  void givenObjectWhenSetLastModifiedAtThenGetLastModifiedAtReturnsNewValue() {
    var testObject = new TestObject();
    var newLastModifiedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

    testObject.setLastModifiedAt(newLastModifiedAt);

    assertEquals(newLastModifiedAt, testObject.getLastModifiedAt());
  }

  @Test
  @DisplayName("given object, when setModifiedBy then getModifiedBy returns new value")
  void givenObjectWhenSetModifiedByThenGetModifiedByReturnsNewValue() {
    var testObject = new TestObject();

    testObject.setModifiedBy("jane.doe");

    assertEquals("jane.doe", testObject.getModifiedBy());
  }
}
