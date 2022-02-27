package gameObjects;

import util.Rect;
import java.awt.Color;
import java.awt.Graphics;

import main.entry;

public class ColorRect extends GameObject {
	String color;
	public ColorRect(double x, double y, double width, double height, double depth, String color) {
		super(x, y, width, height, depth);
		
		this.color = color;
	}

	float c = 0.05f;
	@Override
	public void paint(Graphics g, Rect r) {
		if(!entry.app.colors.containsKey(color)) {
			g.setColor(Color.RED);
			g.fillRect((int)Math.floor(r.getX()-c), (int)Math.floor(r.getY()-c), (int)Math.ceil(r.getWidth()+c), (int)Math.ceil(r.getHeight()+c));
		}else {
			g.setColor(entry.app.colors.get(color));
			g.fillRect((int)Math.floor(r.getX()-c), (int)Math.floor(r.getY()-c), (int)Math.ceil(r.getWidth()+c), (int)Math.ceil(r.getHeight()+c));
		}
	}

	public String getColor(){
		return color;
	}

}
