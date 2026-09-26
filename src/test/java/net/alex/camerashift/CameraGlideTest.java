package net.alex.camerashift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CameraGlideTest {
    @Test
    void fullGlideFinishesAfterTheFullDuration() {
        CameraGlide glide = new CameraGlide();
        glide.snapTo(0.0);
        glide.glideTo(1.0, 1000, 400);
        assertFalse(glide.isFinished(1399));
        assertTrue(glide.isFinished(1400));
        assertEquals(1.0, glide.easedProgress(1400), 1e-9);
        assertEquals(1.0, glide.easedProgress(5000), 1e-9);
    }

    @Test
    void easingStartsSlowAndMeetsTheMidpoint() {
        CameraGlide glide = new CameraGlide();
        glide.snapTo(0.0);
        glide.glideTo(1.0, 0, 400);
        assertEquals(0.0, glide.easedProgress(0), 1e-9);
        assertTrue(glide.easedProgress(40) < 0.1);
        assertEquals(0.5, glide.easedProgress(200), 1e-9);
    }

    @Test
    void reversingMidGlideContinuesFromTheCurrentPosition() {
        CameraGlide glide = new CameraGlide();
        glide.snapTo(1.0);
        glide.glideTo(0.0, 0, 400);
        double before = glide.easedProgress(100);
        glide.glideTo(1.0, 100, 400);
        assertEquals(before, glide.easedProgress(100), 1e-9);
        // A quarter of the span is left to undo, so the way back takes a quarter of the duration.
        assertFalse(glide.isFinished(199));
        assertTrue(glide.isFinished(200));
        assertEquals(1.0, glide.target());
    }

    @Test
    void snapHoldsStillAndCountsAsFinished() {
        CameraGlide glide = new CameraGlide();
        glide.snapTo(0.0);
        assertTrue(glide.isFinished(0));
        assertEquals(0.0, glide.easedProgress(12345), 1e-9);
    }
}
