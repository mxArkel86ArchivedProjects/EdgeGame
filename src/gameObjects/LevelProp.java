package gameObjects;

import java.awt.Graphics;
import main.entry;
import util.Rect;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class LevelProp extends GameObject {
	String asset;
	
	public LevelProp(double x, double y, double width, double height, double depth, String asset) {
		super(x, y, width, height, depth);
		
		this.asset = asset;
	}
	@Override
	public void paint(Graphics g, Rect r) {
		if(!entry.app.assets.containsKey(asset)) {
			g.setColor(Color.RED);
			g.fillRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
		}else {
			g.drawImage(entry.app.assets.get(asset), (int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight(), null);
		}
	}

}
