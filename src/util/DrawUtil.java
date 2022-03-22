package util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawUtil {
	public static void DrawRect(Graphics2D g, Rect r, Color c) {
		g.setColor(c);
		g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
	}

	public static void DrawInvertedPolygon(List<Polygon> polygons, Graphics2D g, int width, int height) {
		List<Integer> x = new ArrayList<Integer>();
		List<Integer> y = new ArrayList<Integer>();
		for (Polygon p : polygons) {
			for (int i : p.xpoints)
				x.add(i);
			for (int i : p.ypoints)
				y.add(i);
			x.add(width / 2);
			y.add(height / 2);
		}
		x.addAll(Arrays.asList(0, width, width, 0, 0));
		y.addAll(Arrays.asList(0, 0, height, height, 0));
		int x_[] = x.stream()
		.mapToInt(Integer::intValue)
		.toArray();
		int y_[] = y.stream()
		.mapToInt(Integer::intValue)
				.toArray();
		
		g.fillPolygon(x_, y_, x.size());
	}
}
