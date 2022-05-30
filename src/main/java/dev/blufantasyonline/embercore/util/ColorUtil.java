package dev.blufantasyonline.embercore.util;

import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.math.MathUtil;

import java.awt.*;

public final class ColorUtil {
    /**
     * Returns an array containing <code>numColors</code> {@link Color} objects. Those at the beginning and end
     * of the array will always be copies of the provided <code>from</code> and <code>to</code> colors, respectively.
     */
    public static Color[] gradient(Color from, Color to, int numColors) {
        // Don't bother trying to compute anything for anything less than 3 colors,
        // just return these.
        if (numColors <= 1)
            return new Color[]{from};
        else if (numColors == 2)
            return new Color[]{from, to};

        Color[] colors = new Color[numColors];
        for (int i = 0; i < numColors; i++) {
            // calculate this against the actual maximum index, rather than
            // the size of the resulting array. this way, we'll actually lerp all the way to the
            // "to" color instead of just before it.
            double t = (double) i / (numColors - 1);
            // lerp between current and target RGB values
            float red = (float) MathUtil.lerp(from.getRed(), to.getRed(), t) / 255.0f;
            float green = (float) MathUtil.lerp(from.getGreen(), to.getGreen(), t) / 255.0f;
            float blue = (float) MathUtil.lerp(from.getBlue(), to.getBlue(), t) / 255.0f;

            colors[i] = new Color(red, green, blue);
        }
        return colors;
    }
}
