package util.templates;

import java.awt.image.BufferedImage;

public class RotateReturn {
    public Rect newsize;
    public Rect oldsize;
    public double angle;
    public BufferedImage out;

    public double offsetX() {
        return (oldsize.width - newsize.width)/2;
    }

    public double offsetY() {
        return (oldsize.height - newsize.height)/2;
    }
}
