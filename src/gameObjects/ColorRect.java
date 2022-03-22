package gameObjects;

public class ColorRect extends GameObject {
	String color;
	public ColorRect(double x, double y, double width, double height, double depth, String color) {
		super(x, y, width, height, depth);
		
		this.color = color;
	}

	public String getColor(){
		return color;
	}

}
