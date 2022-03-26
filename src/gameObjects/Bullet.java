package gameObjects;

import main.Globals;
import util.Rect;

public class Bullet extends Rect {
    public double angle;
    public double speed;

    public Bullet(double x, double y, double size, double angle, double speed) {
        super(x-size/2, y-size/2, size, size);
        this.angle = angle;
        this.speed = speed;
    }

    
}
