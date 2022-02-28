package gameObjects;

import main.Globals;
import util.Rect;

public class Bullet extends Rect {
    double angle;

    public Bullet(double x, double y, double angle) {
        super(x-Globals.BULLET_SIZE/2, y-Globals.BULLET_SIZE/2, Globals.BULLET_SIZE, Globals.BULLET_SIZE);
        this.angle = angle;
    }

    public double getAngle(){
        return angle;
    }
    
}
