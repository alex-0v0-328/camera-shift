package net.alex.camerashift;

/**
 * Time-driven glide of the third-person camera between the eye (progress 0) and its full distance
 * (progress 1). Retargeting mid-glide starts from the current progress, so the camera never jumps
 * when Epic Fight flips modes faster than a glide finishes.
 *
 * <p>{@link #snapTo} holds the camera still at a given progress. {@link #glideTo} shrinks the duration with the
 * remaining span, so reversing halfway through takes half of the full duration. {@link #getEasedProgress} is
 * smoothstep over the linear progress, so a full glide eases in and out at both ends.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public final class CameraGlide {

    private double origin = 1.0;
    private double target = 1.0;
    private long startMillis;
    private long durationMillis;

    public void snapTo(double progress) {
        origin = progress;
        target = progress;
        durationMillis = 0;
    }

    public void glideTo(double target, long nowMillis, long fullDurationMillis) {
        origin = getLinearProgress(nowMillis);
        this.target = target;
        startMillis = nowMillis;
        durationMillis = Math.round(Math.abs(target - origin) * fullDurationMillis);
    }

    public double getTarget() {
        return target;
    }

    public boolean isFinished(long nowMillis) {
        return nowMillis - startMillis >= durationMillis;
    }

    public double getEasedProgress(long nowMillis) {
        double progress = getLinearProgress(nowMillis);
        return progress * progress * (3.0 - 2.0 * progress);
    }

    double getLinearProgress(long nowMillis) {
        if (durationMillis <= 0) {
            return target;
        }
        double fraction = Math.clamp((nowMillis - startMillis) / (double) durationMillis, 0.0, 1.0);
        return origin + (target - origin) * fraction;
    }
}
