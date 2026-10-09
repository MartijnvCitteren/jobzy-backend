package app.jobzy.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseObjectTest {

  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 9, 0);

  private static class TestObject extends BaseObject {
    TestObject() {
      super(CREATED_AT);
    }
  }

  @Test
  @DisplayName(
      "given createdAt, when constructed then createdAt and lastModifiedAt are both that time")
  void givenCreatedAtWhenConstructedThenCreatedAtAndLastModifiedAtAreBothThatTime() {
    var testObject = new TestObject();

    assertEquals(CREATED_AT, testObject.getCreatedAt());
    assertEquals(CREATED_AT, testObject.getLastModifiedAt());
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
