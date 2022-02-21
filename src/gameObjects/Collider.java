package gameObjects;

import util.Rect;

public class Collider extends Rect {
	boolean visible;
	public Collider(double x, double y, double width, double height, boolean visible) {
		super(x, y, width, height);
		
		this.visible = visible;
	}

	public boolean visible(){
		return visible;
	}

}
