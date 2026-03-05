package muon.app.util;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;

import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;

@Slf4j
public final class ScalingUtil {
    private ScalingUtil() {
    }

    private static volatile float scaleFactor = -1f;

    public static float getScaleFactor() {
        float sf = scaleFactor;
        if (sf > 0f) return sf;

        synchronized (ScalingUtil.class) {
            if (scaleFactor > 0f) return scaleFactor;

            if (App.getCONTEXT().getSettings().isManualScaling()) {
                float manual = (float) App.getCONTEXT().getSettings().getUiScaling(); // <- your value
                scaleFactor = roundToCommonScaling(manual);
                log.info("Using MANUAL scale factor: {}", scaleFactor);
                return scaleFactor;
            }

            try {
                // Respect explicit JVM arg if present
                String uiScale = System.getProperty("sun.java2d.uiScale");
                if (uiScale != null && !uiScale.isBlank()) {
                    try {
                        scaleFactor = Float.parseFloat(uiScale.trim());
                        log.info("Using sun.java2d.uiScale property: {}", scaleFactor);
                        return scaleFactor;
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid sun.java2d.uiScale: {}", uiScale);
                    }
                }

                // macOS Retina rendering looks oversized with our defaults; default to 1.0 unless user overrides
                boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
                if (mac) {
                    scaleFactor = 1.0f;
                    log.info("macOS detected; defaulting scale factor to {}", scaleFactor);
                    return scaleFactor;
                }

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gd != null ? gd.getDefaultConfiguration() : null;

                double transformScale = 1.0;
                if (gc != null) {
                    AffineTransform at = gc.getDefaultTransform();
                    transformScale = Math.max(at.getScaleX(), at.getScaleY());
                    log.info("Graphics transform scale: {}", transformScale);
                }

                // DPI fallback
                int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                float dpiScale = dpi / 96.0f;
                log.info("DPI-based scale: {}", dpiScale);

                // On Linux fractional setups, DPI often wins
                boolean linux = System.getProperty("os.name", "").toLowerCase().contains("linux");
                float detected;
                if (linux) {
                    if (Math.abs(dpiScale - transformScale) > 0.1f) detected = dpiScale;
                    else detected = (float) transformScale;
                } else {
                    detected = (float) Math.max(transformScale, dpiScale);
                }

                detected = roundToCommonScaling(detected);

                scaleFactor = Math.max(1.0f, detected);
                log.info("Final detected scale factor: {}", scaleFactor);
            } catch (Throwable t) {
                log.error("Error determining scale factor", t);
                scaleFactor = 1.0f;
            }
            return scaleFactor;
        }
    }

    private static float roundToCommonScaling(float scale) {
        if (scale >= 1.875f) return 2.0f;   // [1.875, ∞) -> 2.0
        if (scale >= 1.625f) return 1.75f;  // [1.625, 1.875)
        if (scale >= 1.375f) return 1.50f;  // [1.375, 1.625)
        if (scale >= 1.125f) return 1.25f;  // [1.125, 1.375)
        return 1.0f;
    }

    public static int scale(int value) {
        return Math.round(value * getScaleFactor());
    }

    public static float scale(float value) {
        return value * getScaleFactor();
    }

    public static Dimension scale(Dimension d) {
        return new Dimension(scale(d.width), scale(d.height));
    }

    public static Insets scale(Insets insets) {
        return scaleInsets(insets.top, insets.left, insets.bottom, insets.right);
    }

    public static Insets scaleInsets(int top, int left, int bottom, int right) {
        return new Insets(scale(top), scale(left), scale(bottom), scale(right));
    }

    public static EmptyBorder getScaledEmptyBorder(int t, int l, int b, int r) {
        return new EmptyBorder(scale(t), scale(l), scale(b), scale(r));
    }

    public static MatteBorder getScaledMatteBorder(int t, int l, int b, int r, Color color) {
        return new MatteBorder(scale(t), scale(l), scale(b), scale(r), color);
    }
}
