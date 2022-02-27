package util;

public class CollisionUtil {
	public static CollisionReturn DynamicCollision(Rect a, Rect b, double dx, double dy) {
		CollisionReturn ret = new CollisionReturn();

		int intent_x = 0;
		if(dx>0.01)
			intent_x = 1;
		else if(dx<-0.01)
			intent_x = -1;
		
		int intent_y = 0;
		if(dy>0.01)
			intent_y = 1;
		else if(dy<-0.01)
			intent_y = -1;

		ret.intent_x = intent_x;
		ret.intent_y = intent_y;

		Point top_left = new Point(a.getX(), a.getY());
		Point top_right =  new Point((a.getX()+a.getWidth()), a.getY());
		Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));
		Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));

		Point object_bottom_left = new Point(b.getX(), (b.getY()+b.getHeight()));
		Point object_bottom_right = new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));
		Point object_top_left = new Point(b.getX(), b.getY());
		Point object_top_right =  new Point((b.getX()+b.getWidth()), b.getY());

		boolean left_intersect = top_right.getX() >= object_bottom_left.getX() && top_left.getX() < object_bottom_left.getX();
		boolean right_intersect = top_left.getX() <= object_bottom_right.getX() && top_right.getX() > object_bottom_right.getX();
		boolean center_intersect_x = top_left.getX() >= object_bottom_left.getX() && top_right.getX() <= object_bottom_right.getX();
		boolean pass_by_x = top_right.getX() <= object_bottom_left.getX() && top_left.getX() + dx >= object_bottom_right.getX();

		boolean getY_intersect = bottom_right.getY() >= object_top_left.getY() && top_left.getY() < object_top_left.getY();
		boolean bottom_intersect = top_left.getY() <= object_bottom_right.getY() && bottom_right.getY() > object_bottom_right.getY();
		boolean center_intersect_y = bottom_right.getY() <= object_bottom_right.getY() && top_left.getY() >= object_top_left.getY();
		boolean pass_by_y = bottom_right.getY() <= object_top_left.getY() && top_left.getY() - dy >= object_bottom_right.getY();

		boolean inline_y = left_intersect || right_intersect || center_intersect_x;
		boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;

		if (intent_x == 1 && intent_y == 1) {// quadrant I
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
				
			}
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}
			return ret;
		}
		else if (intent_x == 0 && intent_y == 1) {// y axis up
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
			}
			return ret;
		}
		else if (intent_x == -1 && intent_y == 1) {// quadrant II
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
			}
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(a.getX() - (b.getX()+b.getWidth()));
			}
			return ret;
		}
		else if (intent_x == -1 && intent_y == 0) {// x axis left
			// check if object is valid before move
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.ceil((b.getX()+b.getWidth()) - a.getX());
			}
			return ret;
		}
		else if (intent_x == -1 && intent_y == -1) {// quadrant III
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil((a.getY()+a.getHeight()) - b.getY());
			}
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(a.getX() - (b.getX()+b.getWidth()));
			}
			return ret;
		}
		else if (intent_x == 0 && intent_y == -1) {// y axis down
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.floor(b.getY() - (a.getY()+a.getHeight()));
			}
			return ret;
		}
		else if (intent_x == 1 && intent_y == -1) {// quadrant IV
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil((a.getY()+a.getHeight()) - b.getY());
			}
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}
			return ret;
		}
		else if (intent_x == 1 && intent_y == 0) {// x axis right
			// check if object is valid before move
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}
			return ret;
		}
		return ret;
	}

	public static boolean staticCollision(Rect a, Rect b) {
		boolean inline_x = (a.getY() <= (b.getY()+b.getHeight()) && a.getY() >= b.getY())
			|| ((a.getY()+a.getHeight()) <= (b.getY()+b.getHeight()) && (a.getY()+a.getHeight()) >= b.getY()) || (a.getY() <= b.getY() && (a.getY()+a.getHeight()) >= (b.getY()+b.getHeight()));
		boolean inline_y = (a.getX() <= (b.getX()+b.getWidth()) && a.getX() >= b.getX())
			|| ((a.getX()+a.getWidth()) <= (b.getX()+b.getWidth()) && (a.getX()+a.getWidth()) >= b.getX()) || (a.getX() <= b.getX() && (a.getX()+a.getWidth()) >= (b.getX()+b.getWidth()));
		if (inline_x && inline_y) {
			return true;
		}
		return false;
	}
}
