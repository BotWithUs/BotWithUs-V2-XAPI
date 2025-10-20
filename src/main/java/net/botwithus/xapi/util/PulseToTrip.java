package net.botwithus.xapi.util;

import net.botwithus.xapi.util.time.Timer;

/**
 * A utility class that tracks pulses and executes an action after a certain number of pulses
 * within a timeout period. Useful for detecting stuck states or triggering fallback actions.
 *
 * This is the v2 xAPI port of the v1 PulseToTrip utility.
 */
public class PulseToTrip {
    private int currentPulseCount;
    private final int targetPulseCount;
    private final Timer timeoutTimer;
    private boolean hasTripped = false;
    private final Runnable tripAction;

    /**
     * Creates a new PulseToTrip instance.
     *
     * @param targetPulseCount The number of pulses needed to trigger the trip action
     * @param timeoutMs The timeout in milliseconds for the pulse counting window
     * @param tripAction The action to execute when the conditions are met
     */
    public PulseToTrip(int targetPulseCount, int timeoutMs, Runnable tripAction) {
        this.targetPulseCount = targetPulseCount;
        this.timeoutTimer = new Timer(timeoutMs, timeoutMs);
        this.tripAction = tripAction;
        this.currentPulseCount = 0;
        this.hasTripped = false;
    }

    /**
     * Sends a pulse to the counter. If enough pulses are received within the timeout
     * period, the trip condition will be triggered.
     */
    public void sendPulse() {
        // Start timer on first pulse
        if (!timeoutTimer.hasStarted()) {
            timeoutTimer.start();
            currentPulseCount = 1;
        } else if (timeoutTimer.hasExpired()) {
            // Timeout expired, reset the counter and start over
            timeoutTimer.reset();
            currentPulseCount = 1;
            hasTripped = false;
        } else {
            // Timer is active and hasn't expired, increment pulse count
            currentPulseCount++;
        }

        // Check if we've reached the target pulse count
        if (currentPulseCount >= targetPulseCount) {
            hasTripped = true;
        }
    }

    /**
     * Executes the trip action if the trip condition has been met.
     * This should be called after checking hasTripped().
     */
    public void runTripAction() {
        if (tripAction != null) {
            tripAction.run();
        }
    }

    /**
     * Checks if the trip condition has been triggered.
     *
     * @return true if enough pulses have been received within the timeout window
     */
    public boolean hasTripped() {
        return hasTripped;
    }

    /**
     * Resets the pulse counter and trip state.
     */
    public void reset() {
        currentPulseCount = 0;
        hasTripped = false;
        timeoutTimer.stop();
    }

    /**
     * Gets the current pulse count.
     *
     * @return The number of pulses received in the current window
     */
    public int getCurrentPulseCount() {
        return currentPulseCount;
    }

    /**
     * Gets the target pulse count needed to trigger the trip.
     *
     * @return The target pulse count
     */
    public int getTargetPulseCount() {
        return targetPulseCount;
    }

    /**
     * Gets the remaining time in the current timeout window.
     *
     * @return Remaining time in milliseconds, or 0 if timer hasn't started
     */
    public long getRemainingTime() {
        if (!timeoutTimer.hasStarted()) {
            return 0;
        }
        return timeoutTimer.getRemainingTime();
    }
}