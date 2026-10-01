package net.alex.camerashift;

/**
 * Time-driven glide of the third-person camera between the eye (progress 0) and its full distance
 * (progress 1). Retargeting mid-glide starts from the current progress, so the camera never jumps
 * when Epic Fight flips modes faster than a glide finishes.
 *
 * <p>{@link #snapTo} holds the camera still at a progress. {@link #glideTo} shrinks the duration with the
 * remaining span, so reversing halfway through takes half of the full duration. {@link #easedProgress} is
 * smoothstep over the linear progress, so a full glide eases in and out at both ends.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public final class CameraGlide {

    private double from = 1.0;
    private double to = 1.0;
    private long startMillis;
    private long durationMillis;

    public void snapTo(double progress) {
        from = progress;
        to = progress;
        durationMillis = 0;
    }

    public void glideTo(double target, long nowMillis, long fullDurationMillis) {
        from = linearProgress(nowMillis);
        to = target;
        startMillis = nowMillis;
        durationMillis = Math.round(Math.abs(target - from) * fullDurationMillis);
    }

    public double target() {
        return to;
    }

    public boolean isFinished(long nowMillis) {
        return nowMillis - startMillis >= durationMillis;
    }

    public double easedProgress(long nowMillis) {
        double p = linearProgress(nowMillis);
        return p * p * (3.0 - 2.0 * p);
    }

    double linearProgress(long nowMillis) {
        if (durationMillis <= 0) {
            return to;
        }
        double t = Math.clamp((nowMillis - startMillis) / (double) durationMillis, 0.0, 1.0);
        return from + (to - from) * t;
    }
}
