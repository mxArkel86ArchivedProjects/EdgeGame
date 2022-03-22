package gameObjects;

import util.Rect;

public abstract class GameObject extends Rect {
	public GameObject(double x, double y, double width, double height, double depth) {
		super(x, y, width, height);
		
		this.depth = depth;
	}
	double depth;

	public double getZ() {
		return depth;
	}
}
