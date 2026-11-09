package Aufgabe_Door;

import java.util.Timer;
import java.util.TimerTask;

public class Door {

    public static class DoorException extends Exception {
        public enum ErrorCode {
            ALREADY_OPEN,    // open()   quand état == OPEN
            ALREADY_CLOSED,  // close()  quand état == CLOSED
            ALREADY_LOCKED,  // lock()   quand état == LOCKED
            IS_LOCKED,       // open()   ou close() quand état == LOCKED
            IS_OPEN,         // lock()   ou unlock() quand état == OPEN
            NOT_LOCKED,      // unlock() quand état == CLOSED
            WRONG_PIN        // unlock() avec mauvais PIN
        }

        private final ErrorCode errorCode;

        public DoorException(ErrorCode errorCode) {
            super(errorCode.name());      // message lisible = nom de l'enumeration
            this.errorCode = errorCode;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }
    }

    public enum State {
        OPENED,
        CLOSED,
        LOCKED
    }


    private volatile State state;
    private int openCount;

    private Timer autoLockTimer;

    /**
     *Fermeture automatique apres 5 sec
     */
    public Door() {
        this.state= State.CLOSED;
        this.openCount = 0;

    }

    public void open() throws DoorException {

        switch (state) {

            case OPENED ->
                    throw new DoorException(
                            DoorException.ErrorCode.ALREADY_OPEN);

            case LOCKED ->
                    throw new DoorException(
                            DoorException.ErrorCode.IS_LOCKED);

            case CLOSED -> {
               cancelAutoLockTimer();
                state = State.OPENED;
                openCount++;
            }
        }
    }

    public void close() throws DoorException {

        switch (state) {

            case CLOSED ->
                    throw new DoorException(
                            DoorException.ErrorCode.ALREADY_CLOSED);

            case LOCKED ->
                    throw new DoorException(
                            DoorException.ErrorCode.IS_LOCKED);

            case OPENED -> {
                state = State.CLOSED;
                scheduleAutoLock();
            }
        }
    }

    public void lock() throws DoorException {

        switch (state) {

            case OPENED ->
                    throw new DoorException(
                            DoorException.ErrorCode.IS_OPEN);

            case LOCKED ->
                    throw new DoorException(
                            DoorException.ErrorCode.ALREADY_LOCKED);

            case CLOSED -> {
                cancelAutoLockTimer();
                state = State.LOCKED;
            }
        }
    }

    public void unlock(int providedPin) throws DoorException {

        switch (state) {

            case OPENED ->
                    throw new DoorException(
                            DoorException.ErrorCode.IS_OPEN);

            case CLOSED ->
                    throw new DoorException(
                            DoorException.ErrorCode.NOT_LOCKED);

            case LOCKED -> {

                int pin = 1234;
                if (pin != providedPin) {
                    throw new DoorException(
                            DoorException.ErrorCode.WRONG_PIN);
                }

                state = State.CLOSED;
                scheduleAutoLock();
            }
        }
    }

    public final State getState() {
        return state;
    }

    public final int getOpenCount() {
        return openCount;
    }

    //cette methode lance automatiquement le timer de verrouillage
    private void scheduleAutoLock() {

        cancelAutoLockTimer();

        autoLockTimer = new Timer("door-auto-lock", true);

        long autoLockDelayMs = 500;
        autoLockTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                if (state == State.CLOSED) {
                    state = State.LOCKED;
                }
            }

        }, autoLockDelayMs);
    }

    //interrompt un timer en cours
    private void cancelAutoLockTimer() {

        if (autoLockTimer != null) {
            autoLockTimer.cancel();
            autoLockTimer = null;
        }
    }
}
