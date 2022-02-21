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

		if (intent_x == 1 && intent_y == 1) {// quadrant I
			Point getY_left = new Point(a.getX(), a.getY());
			Point getY_right =  new Point((a.getX()+a.getWidth()), a.getY());
			Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));

			Point object_bottom_left = new Point(b.getX(), (b.getY()+b.getHeight()));
			Point object_bottom_right = new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));
			Point object_getY_left = new Point(b.getX(), b.getY());

			boolean left_intersect = getY_right.getX() >= object_bottom_left.getX() && getY_left.getX() < object_bottom_left.getX();
			boolean right_intersect = getY_left.getX() <= object_bottom_right.getX() && getY_right.getX() > object_bottom_right.getX();
			boolean center_intersect_x = getY_left.getX() >= object_bottom_left.getX() && getY_right.getX() <= object_bottom_right.getX();
			boolean pass_by_x = getY_right.getX() <= object_bottom_left.getX() && getY_left.getX() + dx >= object_bottom_right.getX();

			boolean getY_intersect = bottom_right.getY() >= object_getY_left.getY() && getY_left.getY() < object_getY_left.getY();
			boolean bottom_intersect = getY_left.getY() <= object_bottom_right.getY() && bottom_right.getY() > object_bottom_right.getY();
			boolean center_intersect_y = bottom_right.getY() <= object_bottom_right.getY() && getY_left.getY() >= object_getY_left.getY();
			boolean pass_by_y = bottom_right.getY() <= object_getY_left.getY() && getY_left.getY() - dy >= object_bottom_right.getY();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
				return ret;
			}
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}
		}
		else if (intent_x == 0 && intent_y == 1) {// y axis up
			Point getY_left =  new Point(a.getX(), a.getY());
			Point getY_right =  new Point((a.getX()+a.getWidth()), a.getY());

			Point object_bottom_left =  new Point(b.getX(), (b.getY()+b.getHeight()));
			Point object_bottom_right =  new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));

			boolean left_intersect = getY_right.getX() >= object_bottom_left.getX() && getY_left.getX() < object_bottom_left.getX();
			boolean right_intersect = getY_left.getX() <= object_bottom_right.getX() && getY_right.getX() > object_bottom_right.getX();
			boolean center_intersect_x = getY_left.getX() >= object_bottom_left.getX() && getY_right.getX() <= object_bottom_right.getX();
			boolean pass_by_x = getY_right.getX() <= object_bottom_left.getX() && getY_left.getX() + dx >= object_bottom_right.getX();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
			}
		}
		else if (intent_x == -1 && intent_y == 1) {// quadrant II
			Point getY_left =  new Point(a.getX(), a.getY());
			Point getY_right =  new Point((a.getX()+a.getWidth()), a.getY());
			Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));

			Point object_bottom_left =  new Point(b.getX(), (b.getY()+b.getHeight()));
			Point object_bottom_right =  new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));
			Point object_getY_right =  new Point((b.getX()+b.getWidth()), b.getY());

			boolean left_intersect = getY_right.getX() >= object_bottom_left.getX() && getY_left.getX() < object_bottom_left.getX();
			boolean right_intersect = getY_left.getX() <= object_bottom_right.getX() && getY_right.getX() > object_bottom_right.getX();
			boolean center_intersect_x = getY_left.getX() >= object_bottom_left.getX() && getY_right.getX() <= object_bottom_right.getX();
			boolean pass_by_x = getY_right.getX() <= object_bottom_left.getX() && getY_left.getX() + dx >= object_bottom_right.getX();

			boolean getY_intersect = bottom_left.getY() >= object_getY_right.getY() && getY_left.getY() < object_getY_right.getY();
			boolean bottom_intersect = getY_left.getY() <= object_bottom_right.getY() && bottom_left.getY() > object_bottom_right.getY();
			boolean center_intersect_y = bottom_left.getY() <= object_bottom_right.getY() && getY_left.getY() >= object_getY_right.getY();
			boolean pass_by_y = bottom_left.getY() <= object_getY_right.getY() && getY_left.getY() - dy >= object_bottom_right.getY();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.getY() <= (b.getY()+b.getHeight()) && a.getY() + dy > (b.getY()+b.getHeight())) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil(a.getY() - (b.getY()+b.getHeight()));
			}
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(a.getX() - (b.getX()+b.getWidth()));
			}
		}
		else if (intent_x == -1 && intent_y == 0) {// x axis left
			Point getY_left =  new Point(a.getX(), a.getY());
			Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));

			Point object_getY_right =  new Point((b.getX()+b.getWidth()), b.getY());
			Point object_bottom_right =  new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));

			boolean getY_intersect = bottom_left.getY() >= object_getY_right.getY() && getY_left.getY() < object_getY_right.getY();
			boolean bottom_intersect = getY_left.getY() <= object_bottom_right.getY() && bottom_left.getY() > object_bottom_right.getY();
			boolean center_intersect_y = bottom_left.getY() <= object_bottom_right.getY() && getY_left.getY() >= object_getY_right.getY();
			boolean pass_by_y = bottom_left.getY() <= object_getY_right.getY() && getY_left.getY() - dy >= object_bottom_right.getY();

			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.ceil((b.getX()+b.getWidth()) - a.getX());
			}
		}
		else if (intent_x == -1 && intent_y == -1) {// quadrant III
			Point getY_left =  new Point(a.getX(), a.getY());
			Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));
			Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));

			Point object_getY_left =  new Point(b.getX(), b.getY());
			Point object_getY_right =  new Point((b.getX()+b.getWidth()), b.getY());
			Point object_bottom_right =  new Point((b.getX()+b.getWidth()), (b.getY()+b.getHeight()));

			boolean left_intersect = bottom_right.getX() >= object_getY_left.getX() && getY_left.getX() < object_getY_left.getX();
			boolean right_intersect = getY_left.getX() <= object_bottom_right.getX() && bottom_right.getX() > object_bottom_right.getX();
			boolean center_intersect_x = getY_left.getX() >= object_getY_left.getX() && bottom_right.getX() <= object_bottom_right.getX();
			boolean pass_by_x = bottom_right.getX() <= object_getY_left.getX() && getY_left.getX() + dx >= object_bottom_right.getX();

			boolean getY_intersect = bottom_left.getY() >= object_getY_right.getY() && getY_left.getY() < object_getY_right.getY();
			boolean bottom_intersect = getY_left.getY() <= object_bottom_right.getY() && bottom_left.getY() > object_bottom_right.getY();
			boolean center_intersect_y = bottom_left.getY() <= object_bottom_right.getY() && getY_left.getY() >= object_getY_right.getY();
			boolean pass_by_y = bottom_left.getY() <= object_getY_right.getY() && getY_left.getY() - dy >= object_bottom_right.getY();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil((a.getY()+a.getHeight()) - b.getY());
			}
			if ((inline_x || pass_by_y) && a.getX() >= (b.getX()+b.getWidth()) && a.getX() + dx < (b.getX()+b.getWidth())) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(a.getX() - (b.getX()+b.getWidth()));
			}
		}
		else if (intent_x == 0 && intent_y == -1) {// y axis down
			Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));
			Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));

			Point object_getY_left =  new Point(b.getX(), b.getY());
			Point object_getY_right =  new Point((b.getX()+b.getWidth()), b.getY());

			boolean left_intersect = bottom_right.getX() >= object_getY_left.getX() && bottom_left.getX() < object_getY_left.getX();
			boolean right_intersect = bottom_left.getX() <= object_getY_right.getX() && bottom_right.getX() > object_getY_right.getX();
			boolean center_intersect_x = bottom_left.getX() >= object_getY_left.getX() && bottom_right.getX() <= object_getY_right.getX();
			boolean pass_by_x = bottom_right.getX() <= object_getY_left.getX() && bottom_left.getX() + dx >= object_getY_right.getX();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.floor(b.getY() - (a.getY()+a.getHeight()));
			}
		}
		else if (intent_x == 1 && intent_y == -1) {// quadrant IV
			Point getY_right =  new Point((a.getX()+a.getWidth()), a.getY());
			Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));
			Point bottom_left =  new Point(a.getX(), (a.getY()+a.getHeight()));

			Point object_getY_left =  new Point(b.getX(), b.getY());
			Point object_getY_right =  new Point((b.getX()+b.getWidth()), b.getY());
			Point object_bottom_left =  new Point(b.getX(), (b.getY()+b.getHeight()));

			boolean left_intersect = bottom_right.getX() >= object_getY_left.getX() && bottom_left.getX() < object_getY_left.getX();
			boolean right_intersect = bottom_left.getX() <= object_getY_right.getX() && bottom_right.getX() > object_getY_right.getX();
			boolean center_intersect_x = bottom_left.getX() >= object_getY_left.getX() && bottom_right.getX() <= object_getY_right.getX();
			boolean pass_by_x = bottom_right.getX() <= object_getY_left.getX() && bottom_left.getX() + dx >= object_getY_right.getX();

			boolean getY_intersect = bottom_left.getY() >= object_getY_right.getY() && getY_right.getY() < object_getY_right.getY();
			boolean bottom_intersect = getY_right.getY() <= object_bottom_left.getY() && bottom_left.getY() > object_bottom_left.getY();
			boolean center_intersect_y = bottom_left.getY() <= object_bottom_left.getY() && getY_right.getY() >= object_getY_right.getY();
			boolean pass_by_y = bottom_left.getY() <= object_getY_right.getY() && getY_right.getY() - dy >= object_bottom_left.getY();

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && (a.getY()+a.getHeight()) <= b.getY() && (a.getY()+a.getHeight()) - dy > b.getY()) {
				ret.y_collision = true;
				ret.disp_y = Math.ceil((a.getY()+a.getHeight()) - b.getY());
			}
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}

		}
		else if (intent_x == 1 && intent_y == 0) {// x axis right
			Point getY_right =  new Point((a.getX()+a.getWidth()), a.getY());
			Point bottom_right =  new Point((a.getX()+a.getWidth()), (a.getY()+a.getHeight()));

			Point object_getY_left =  new Point(b.getX(), b.getY());
			Point object_bottom_left =  new Point(b.getX(), (b.getY()+b.getHeight()));

			boolean getY_intersect = bottom_right.getY() >= object_getY_left.getY() && getY_right.getY() < object_getY_left.getY();
			boolean bottom_intersect = getY_right.getY() <= object_bottom_left.getY() && bottom_right.getY() > object_bottom_left.getY();
			boolean center_intersect_y = bottom_right.getY() <= object_bottom_left.getY() && getY_right.getY() >= object_getY_left.getY();
			boolean pass_by_y = bottom_right.getY() <= object_getY_left.getY() && getY_right.getY() - dy >= object_bottom_left.getY();

			boolean inline_x = getY_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_x || pass_by_y) && (a.getX()+a.getWidth()) <= b.getX() && (a.getX()+a.getWidth()) + dx > b.getX()) {
				ret.x_collision = true;
				ret.disp_x = Math.floor(b.getX() - (a.getX()+a.getWidth()));
			}
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
