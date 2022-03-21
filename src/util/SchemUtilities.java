package util;

public class SchemUtilities {

    public static Point schemToLocalPoint(Point r, Point location, double GRIDSIZE) {
    	/// multiply by GRIDSIZE, subtract camera location
    	return new Point(r.getX() * GRIDSIZE - location.x, r.getY() * GRIDSIZE - location.y);
    }

    public static Rect schemToLocal(Rect r, Point location, double GRIDSIZE) {
    	/// multiply by GRIDSIZE, subtract camera location
    	return new Rect(r.getX() * GRIDSIZE - location.x, r.getY() * GRIDSIZE - location.y, r.getWidth() * GRIDSIZE,
    			r.getHeight() * GRIDSIZE);
    }

	public static Rect schemToLocalZ(Rect r, Rect PLAYER_SCREEN_LOC, Point location, double depth, double GRIDSIZE) {
		double z = Math.exp(depth);
    	Point p = new Point(
    			r.getX() * GRIDSIZE * z - location.x * z - (PLAYER_SCREEN_LOC.getX() * z - PLAYER_SCREEN_LOC.getX())
    					+ (r.getWidth() * GRIDSIZE / 2 * z - r.getWidth() * GRIDSIZE / 2)
    					- (PLAYER_SCREEN_LOC.getWidth() / 2 * z - PLAYER_SCREEN_LOC.getWidth() / 2),
    			r.getY() * GRIDSIZE * z - location.y * z - PLAYER_SCREEN_LOC.getY() * z + PLAYER_SCREEN_LOC.getY()
    					+ r.getHeight() * GRIDSIZE * z - r.getHeight() * GRIDSIZE - PLAYER_SCREEN_LOC.getHeight() * z
    					+ PLAYER_SCREEN_LOC.getHeight());
    	return new Rect(p.x, p.y, r.getWidth() * GRIDSIZE, r.getHeight() * GRIDSIZE);
    }

    public static Point schemPointFromFramePos(Point p, Point location, double GRIDSIZE) {
    	return new Point(Math.round((p.getX() + location.getX()) / GRIDSIZE),
    			Math.round((p.getY() + location.getY()) / GRIDSIZE));
    }
    
}
