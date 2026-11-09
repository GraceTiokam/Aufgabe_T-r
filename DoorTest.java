package Aufgabe_Door;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Door - Zustand-Tests")
public class DoorTest {
    private static final int VALID_PIN = 1234;
    private static final int WRONG_PIN = 4321;

    private static final long DELAY_MS = 500L;

    // 1. GÜLTIGE ZUSTANDSÜBERGÄNGE

    @Test
    @DisplayName("CLOSED -> OPEN")
    public void closedToOpen() {

        Door door = closedDoor();

        assertDoesNotThrow(door::open);

        assertEquals(Door.State.OPENED, door.getState());
    }

    @Test
    @DisplayName("OPEN -> CLOSED")
    public void openToClosed() {

        Door door = openDoor();

        assertDoesNotThrow(door::close);

        assertEquals(Door.State.CLOSED, door.getState());
    }

    @Test
    @DisplayName("CLOSED -> LOCKED")
    public void closedToLocked() {

        Door door = closedDoor();

        assertDoesNotThrow(door::lock);

        assertEquals(Door.State.LOCKED, door.getState());
    }

    @Test
    @DisplayName("LOCKED -> CLOSED mit korrektem PIN")
    public void lockedToClosedWithCorrectPin() {

        Door door = lockedDoor();

        assertDoesNotThrow(() -> door.unlock(VALID_PIN));

        assertEquals(Door.State.CLOSED, door.getState());
    }

    @Test
    @DisplayName("CLOSED -> LOCKED durch Auto-Lock")
    public void closedToLockedByAutoLock() throws InterruptedException {

        Door door = openDoor();

        assertDoesNotThrow(door::close);

        Thread.sleep(DELAY_MS + 50);

        assertEquals(Door.State.LOCKED, door.getState());
    }


    // 2. AUTO-LOCK

    @Test
    @DisplayName("Öffnen vor Timeout bricht Auto-Lock ab")
    public void openBeforeTimeoutCancelsAutoLock() {

        Door door = openDoor();

        assertDoesNotThrow(door::close);   // timer démarre
        assertDoesNotThrow(door::open);    // timer annulé

        sleep();

        assertEquals(Door.State.OPENED, door.getState());
    }

    @Test
    @DisplayName("Nach unlock() startet Auto-Lock erneut")
    public void autoLockRestartsAfterUnlock()
             {

        Door door = lockedDoor();

        assertDoesNotThrow(() -> door.unlock(VALID_PIN));

        sleep();

        assertEquals(Door.State.LOCKED, door.getState());
    }

    // 3. ÖFFNUNGSZÄHLER

    @Test
    @DisplayName("Öffnungszähler beginnt bei 0")
    public void openCountStartsAtZero() {
        assertEquals(0, closedDoor().getOpenCount());
    }

    @Test
    @DisplayName("Öffnungszähler erhöht sich bei jedem erfolgreichen Öffnen")
    public void openCountIncrementsOnEachOpen() {

        Door door = closedDoor();

        assertDoesNotThrow(door::open);
        assertDoesNotThrow(door::close);
        assertDoesNotThrow(door::open);
        assertDoesNotThrow(door::close);

        assertEquals(2, door.getOpenCount());
    }

    @Test
    @DisplayName("Andere Operationen verändern den Öffnungszähler nicht")
    public void openCountUnchangedByOtherOps()
            throws Door.DoorException {

        Door door = closedDoor();

        door.lock();
        door.unlock(VALID_PIN);

        assertEquals(0, door.getOpenCount());
    }

    // 4. UNGÜLTIGE OPERATIONEN - OPEN Paramétrée

    @ParameterizedTest(name = "OPEN + {0} -> DoorException")
    @MethodSource("invalidOpsWhenOpen")
    public void  openStateInvalidOperationThrowsException(
            String description,
            Executable operation,
            Door.DoorException.ErrorCode expectedCode) {
        Door.DoorException ex = assertThrows(Door.DoorException.class, operation);
        assertEquals(expectedCode, ex.getErrorCode(), description);
    }

    private static Stream<Arguments> invalidOpsWhenOpen() {

        return Stream.of(

                Arguments.of(
                        "[MINOR] open() -> already open",
                        (Executable) () -> openDoor().open(),
                        Door.DoorException.ErrorCode.ALREADY_OPEN
                ),

                Arguments.of(
                        "[MINOR] lock() -> door is open",
                        (Executable) () -> openDoor().lock(),
                        Door.DoorException.ErrorCode.IS_OPEN
                ),

                Arguments.of(
                        "[MINOR] unlock() -> door is open",
                        (Executable) () -> openDoor().unlock(VALID_PIN),
                        Door.DoorException.ErrorCode.IS_OPEN
                )
        );
    }

    // 5. UNGÜLTIGE OPERATIONEN - CLOSED Paramétrée

    @ParameterizedTest(name = "CLOSED + {0} -> DoorException")
    @MethodSource("invalidOpsWhenClosed")
    public void closedStateInvalidOperationThrowsException(
            String description,
            Executable operation,
            Door.DoorException.ErrorCode expectedCode) {
        Door.DoorException ex = assertThrows(Door.DoorException.class, operation);
        assertEquals(expectedCode, ex.getErrorCode(), description);
    }

    private static Stream<Arguments> invalidOpsWhenClosed() {

        return Stream.of(

                Arguments.of(
                        "[NORMAL] close() -> déjà fermé",
                        (Executable)
                                () -> closedDoor().close(),
                        Door.DoorException.ErrorCode.ALREADY_CLOSED
                ),

                Arguments.of(
                        "[MINOR] unlock()-> pas verrouillée",
                        (Executable)
                                () -> closedDoor().unlock(VALID_PIN),
                        Door.DoorException.ErrorCode.NOT_LOCKED
                )
        );
    }


    // 6. UNGÜLTIGE OPERATIONEN - LOCKED param


    @ParameterizedTest(name = "LOCKED + {0} -> DoorException")
    @MethodSource("invalidOpsWhenLocked")
    public void lockedStateInvalidOperationThrowsException(
            String description,
            Executable operation,
            Door.DoorException.ErrorCode expectedCode) {
        Door.DoorException ex = assertThrows(Door.DoorException.class, operation);
        assertEquals(expectedCode, ex.getErrorCode(), description);
    }


    private static Stream<Arguments> invalidOpsWhenLocked() {

        return Stream.of(

                Arguments.of(
                        "[CRITICAL] open() -> access denied",
                        (Executable)
                                () -> lockedDoor().open(),
                        Door.DoorException.ErrorCode.IS_LOCKED
                ),

                Arguments.of(
                        "[NORMAL] close() -> door is locked",
                        (Executable)
                                () -> lockedDoor().close(),
                        Door.DoorException.ErrorCode.IS_LOCKED
                ),

                Arguments.of(
                        "[NORMAL] lock() -> already locked",
                        (Executable)
                                () -> lockedDoor().lock(),
                        Door.DoorException.ErrorCode.ALREADY_LOCKED
                ),

                Arguments.of(
                        "[CRITICAL] unlock() -> wrong PIN",
                        (Executable)
                                () -> lockedDoor().unlock(WRONG_PIN),
                        Door.DoorException.ErrorCode.WRONG_PIN
                )
        );
    }



    // 7. FACTORY-METHODEN FÜR TESTS

    /**
     * Erstellt eine neue Tür im Zustand CLOSED.
     */
    private static Door closedDoor() {

        return new Door();
    }

    /**
     * Erstellt eine neue Tür im Zustand OPEN.
     */
    private static Door openDoor() {

        try {

            Door door = closedDoor();

            door.open();

            return door;

        } catch (Door.DoorException e) {

            throw new AssertionError(
                    "Setup fehlgeschlagen: Tür konnte nicht geöffnet werden.",
                    e
            );
        }
    }

    /**
     * Erstellt eine neue Tür im Zustand LOCKED.
     */
    private static Door lockedDoor() {

        try {

            Door door = closedDoor();

            door.lock();

            return door;

        } catch (Door.DoorException e) {

            throw new AssertionError(
                    "Setup fehlgeschlagen: Tür konnte nicht verriegelt werden.",
                    e
            );
        }
    }
    private static void sleep() {
        try {
            Thread.sleep(550L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test unterbrochen: " + e.getMessage());
        }
    }
}
