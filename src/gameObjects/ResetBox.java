package gameObjects;

import util.Rect;

public class ResetBox extends Rect {
    public String checkpoint;
    public ResetBox(double x, double y, double width, double height, String checkpoint) {
        super(x, y, width, height);
        this.checkpoint = checkpoint;
    }
    
}
