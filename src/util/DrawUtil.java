package util;

import java.awt.*;

public class DrawUtil {
	public static void DrawRect(Graphics2D g, Rect r, Color c) {
		g.setColor(c);
		g.fillRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
	}
}
