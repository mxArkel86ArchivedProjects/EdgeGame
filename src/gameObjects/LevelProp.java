package gameObjects;

public class LevelProp extends GameObject {
	String asset;
	
	public LevelProp(double x, double y, double width, double height, double depth, String asset) {
		super(x, y, width, height, depth);
		
		this.asset = asset;
	}

	public String getAsset(){
		return asset;
	}

}
