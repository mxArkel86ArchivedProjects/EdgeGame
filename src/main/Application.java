package main;

import java.awt.image.BufferedImage;
import java.awt.Color;
import util.Rect;
import util.SchemUtilities;
import util.CollisionReturn;
import util.CollisionUtil;
import util.LevelConfigUtil;
import util.MathUtil;
import util.DrawUtil;
import util.ImageImport;
import util.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import java.awt.Font;
import java.awt.BasicStroke;

import gameObjects.Bullet;
import gameObjects.Collider;
import gameObjects.ColorRect;
import gameObjects.GameObject;
import gameObjects.LevelProp;
import gameObjects.ResetBox;

public class Application extends JPanel {
	Rect PLAYER_SCREEN_LOC = null;
	Point TOPLEFT_BOUND = new Point(0, 0);
	Point BOTTOMRIGHT_BOUND = new Point(0, 0);
	Point location = new Point(0, 0);

	public List<Collider> colliders = new ArrayList<Collider>();
	public List<GameObject> objects = new ArrayList<GameObject>();
	public List<ResetBox> resetboxes = new ArrayList<ResetBox>();
	public List<GameObject> newObjects = new ArrayList<GameObject>();
	public List<Collider> newColliders = new ArrayList<Collider>();
	public List<Bullet> bullets = new ArrayList<Bullet>();

	public HashMap<String, Point> checkpoints = new HashMap<String, Point>();
	public HashMap<String, BufferedImage> assets = new HashMap<String, BufferedImage>();
	public HashMap<String, Color> colors = new HashMap<String, Color>();

	Font DEATHSCREEN_TEXT = new Font("Arial", Font.BOLD, 48);
	Font DEBUG_TEXT = new Font("Arial", Font.PLAIN, 12);
	Font UI_TEXT = new Font("Arial", Font.PLAIN, 28);

	double GRIDSIZE = Globals.GRIDSIZE;
	double PLAYER_WIDTH = Globals.PLAYER_WIDTH;
	double PLAYER_HEIGHT = Globals.PLAYER_HEIGHT;
	double PLAYER_SPEED = Globals.PLAYER_SPEED;

	double vertical_velocity = 0;
	boolean grounded = false;
	boolean gridsize_toggle = true;
	boolean DEBUG_ = false;
	double component_x = 0;
	double component_y = 0;
	boolean enabled = false;
	boolean deathscreen = false;
	long deathscreen_tick = 0;
	Point select_point_1 = new Point(0, 0);
	Point select_point_2 = new Point(0, 0);
	boolean selectstage = false;
	char selecttype = 0;
	String selectasset = "void";
	String selectcolor = "black";
	boolean CLIP = false;
	boolean typing = false;
	String typing_str = "";
	double sprint = 100;
	long sprint_tick = 0;

	boolean clip_toggle = true;
	boolean save_toggle = true;
	boolean type_toggle = true;
	boolean enter_toggle = true;
	boolean backspace_toggle = true;
	boolean selecttype_toggle = true;

	public void Init(int width, int height) {
		// update UI to use new width and height
		onResize(width, height);

		// import images into list
		ImageImport.ImportImageAssets();

		// import level object placements
		LevelConfigUtil.loadLevel();

		// place player at "start" location in level
		if (checkpoints.containsKey("start"))
			setPlayerPosFromSchem(checkpoints.get("start"));

		// sort level objects to reflect proper depth in scene
		Collections.sort(objects, new Comparator<GameObject>() {
			@Override
			public int compare(GameObject a, GameObject b) {
				if (a.getZ() > b.getZ())
					return 1;
				else if (a.getZ() == b.getZ())
					return 0;
				else
					return -1;
			}
		});

		// update variables dependent on object placement
		levelUpdate();
	}

	// SET PLAYER POSITION FROM SCHEMATIC COORDINATES
	void setPlayerPosFromSchem(Point p) {
		location = new Point(p.getX() * GRIDSIZE - PLAYER_SCREEN_LOC.getX(),
				p.getY() * GRIDSIZE - PLAYER_SCREEN_LOC.getY() - PLAYER_SCREEN_LOC.getHeight() / 2 - 1.5 * GRIDSIZE);
	}

	// GET SCHEMATIC COORDINATES FROM SCREEN COORDINATES
	Point schemPointFromFramePos(Point p, Point location, double GRIDSIZE) {
		return new Point(Math.round((p.getX() + location.getX()) / GRIDSIZE),
				Math.round((p.getY() + location.getY()) / GRIDSIZE));
	}

	// PAINT
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;

		// check what part of the level is visible on screen
		Rect LEVEL_SCREEN_SPACE = new Rect(
				Math.max(0, (TOPLEFT_BOUND.x) * GRIDSIZE - location.x),
				Math.max(0, (TOPLEFT_BOUND.y) * GRIDSIZE - location.y),
				MathUtil.min_(BOTTOMRIGHT_BOUND.x * GRIDSIZE - location.x,
						getWidth() - TOPLEFT_BOUND.x * GRIDSIZE + location.x,
						(BOTTOMRIGHT_BOUND.x - TOPLEFT_BOUND.x) * GRIDSIZE, getWidth()),
				MathUtil.min_(BOTTOMRIGHT_BOUND.y * GRIDSIZE - location.y,
						getHeight() - TOPLEFT_BOUND.y * GRIDSIZE + location.y,
						(BOTTOMRIGHT_BOUND.y - TOPLEFT_BOUND.y) * GRIDSIZE, getHeight()));

		// clear canvas
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// draw GameObjects from 0 to 1 depth (back)
		for (GameObject o : objects) {
			if (o.getZ() <= 1) {
				if (o instanceof ColorRect) {
					Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				} else if (o instanceof LevelProp) {
					Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				}
			}
		}

		// g.setColor(new Color(30, 30, 30, 160));
		// g.fillRect((int) LEVEL_SCREEN_SPACE.getX(), (int) LEVEL_SCREEN_SPACE.getY(),
		// (int) LEVEL_SCREEN_SPACE.getWidth(), (int) LEVEL_SCREEN_SPACE.getHeight());

		// draw grid
		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(1));

		if (DEBUG_) {
			for (int x1 = (int) TOPLEFT_BOUND.x; x1 < BOTTOMRIGHT_BOUND.x; x1++) {
				for (int y1 = (int) TOPLEFT_BOUND.y; y1 < BOTTOMRIGHT_BOUND.y; y1++) {
					Point p = new Point(x1 * GRIDSIZE - location.x, y1 * GRIDSIZE - location.y);

					Rect r1 = new Rect((int) p.x, (int) p.y, (int) (GRIDSIZE), (int) (GRIDSIZE));
					if (inScreenSpace(r1)) {
						g.drawLine((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.x),
								(int) Math.floor(p.y + GRIDSIZE));// down

						g.drawLine((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.x + GRIDSIZE),
								(int) Math.floor(p.y));// right
					}
				}
			}
		}

		// draw collider lines
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4));
		for (Collider o : colliders) {
			Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
			if (inScreenSpace(r) && o.visible())
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		// draw new colliders
		g.setColor(Color.ORANGE);
		for (Collider o : newColliders) {
			Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);

			if (inScreenSpace(r) && o.visible())
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		// draw player
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));
		DrawUtil.DrawRect(g, PLAYER_SCREEN_LOC, Color.RED);

		// draw bullets
		for (Bullet b : bullets) {
			Rect r = new Rect(b.x - location.x, b.y - location.y, b.width, b.height);
			if (inScreenSpace(r))
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		// draw GameObjects from 1 to infinite depth (front)
		for (GameObject o : objects) {
			if (o.getZ() > 1) {
				if (o instanceof ColorRect) {
					Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				} else if (o instanceof LevelProp) {
					Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				}
			}
		}

		// draw new GameObjects
		for (GameObject o : newObjects) {
			if (o instanceof ColorRect) {
				Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
				if (inScreenSpace(r))
					o.paint(g, r);
			} else if (o instanceof LevelProp) {
				Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
				if (inScreenSpace(r))
					o.paint(g, r);
			}
		}

		// draw player UI
		g.setColor(Color.RED);
		g.fillRect(20, getHeight() - 60, 200, 10);
		g.setColor(Color.YELLOW);
		g.fillRect(20, getHeight() - 40, (int) (200 * sprint / 100), 10);

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.BLACK);
		g.drawRect(20, getHeight() - 60, 200, 10);
		g.drawRect(20, getHeight() - 40, 200, 10);

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(2));

		// draw debug overlay
		if (DEBUG_) {
			for (ResetBox b : resetboxes) {
				Rect r = SchemUtilities.schemToLocal(b, location, GRIDSIZE);
				if (inScreenSpace(r))
					g.drawRect((int) Math.floor(r.getX()), (int) Math.floor(r.getY()), (int) Math.floor(r.getWidth()),
							(int) Math.floor(r.getHeight()));
			}

			g.setColor(Color.BLACK);
			Point mouse = entry.peripherals.mousePos();
			Point schem_mouse = schemPointFromFramePos(mouse, location, GRIDSIZE);
			int z = 3;
			g.fillOval((int) mouse.x - z, (int) mouse.y - z, 2 * z, 2 * z);

			g.setFont(DEBUG_TEXT);
			String focus = selectasset;
			if (selecttype == 2)
				focus = selectcolor;
			g.drawString(
					String.format(
							"raw=(%5.1f,%5.1f)  coord=(%5.1f,%5.1f) grounded=%b component=(%3.1f,%3.1f) velocity=%3.1f stype=%d typing=%b focus=[%s]",
							mouse.x, mouse.y, schem_mouse.x, schem_mouse.y, grounded, component_x, component_y,
							vertical_velocity, (int) selecttype, typing, focus),
					20,
					g.getFontMetrics().getAscent() + 20);

			g.setStroke(new BasicStroke(6));
			g.setColor(Color.BLUE);
			g.drawRect((int) LEVEL_SCREEN_SPACE.getX(), (int) LEVEL_SCREEN_SPACE.getY(),
					(int) LEVEL_SCREEN_SPACE.getWidth(), (int) LEVEL_SCREEN_SPACE.getHeight());

			if (selectstage == true) {
				Point p1 = SchemUtilities.schemToLocalPoint(select_point_1, location, GRIDSIZE);
				Point p2 = SchemUtilities.schemToLocalPoint(
						schemPointFromFramePos(entry.peripherals.mousePos(), location, GRIDSIZE),
						location, GRIDSIZE);
				Rect r = new Rect(p1, p2);

				if (selecttype == 0) {
					g.setColor(Color.RED);
					g.setStroke(new BasicStroke(4));
					g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
				} else if (selecttype == 1) {
					if (assets.containsKey(selectasset))
						g.drawImage(assets.get(selectasset), (int) r.getX(), (int) r.getY(), (int) r.getWidth(),
								(int) r.getHeight(), null);
					else {
						g.setColor(Color.RED);
						g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
					}
				} else if (selecttype == 2) {
					if (colors.containsKey(selectcolor)) {
						g.setColor(colors.get(selectcolor));
						g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(),
								(int) r.getHeight());
					} else {
						g.setColor(Color.RED);
						g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
					}
				}
			}

		}

		// draw text that player is typing out
		if (typing) {
			g.setFont(UI_TEXT);
			g.setColor(Color.RED);
			g.drawString(typing_str, 40, 40);
		}

		// draw deathscreen
		if (deathscreen) {
			int a = 255;
			if (entry.tick - deathscreen_tick > 2000)
				a = 255 - (int) Math.min(255, (entry.tick - deathscreen_tick - 2000) * 255 / 1000);
			// System.out.println("out=" + a);
			g.setFont(DEATHSCREEN_TEXT);
			g.setColor(new Color(0, 0, 0, a));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setStroke(new BasicStroke(2));

			g.setColor(new Color(255, 255, 255, a));
			String str = "Dead";
			Rectangle2D r2d = g.getFontMetrics().getStringBounds(str, 0, str.length(), g);
			g.drawString(str, (int) ((getWidth() - r2d.getWidth()) / 2),
					(int) ((getHeight() + g.getFontMetrics().getAscent()) / 2));

		}
	}

	// RESIZE EVENT
	public void onResize(double width, double height) {
		PLAYER_SCREEN_LOC = new Rect((width - PLAYER_WIDTH) / 2, (height + 0.3 * height - PLAYER_HEIGHT) / 2,
				PLAYER_WIDTH,
				PLAYER_HEIGHT);
	}

	// MOUSE CLICK EVENT
	public void mouseClick(Point pos) {
		if (DEBUG_) {
			if (selectstage == false) {
				select_point_1 = schemPointFromFramePos(pos, location, GRIDSIZE);
				selectstage = true;
			} else if (selectstage == true) {
				select_point_2 = schemPointFromFramePos(pos, location, GRIDSIZE);
				selectstage = false;
				Rect r = new Rect(select_point_1, select_point_2);
				select_point_1 = new Point(0, 0);
				select_point_2 = new Point(0, 0);

				if (selecttype == 0) {
					Collider c = new Collider(r.getX(), r.getY(), r.getWidth(), r.getHeight(), true);
					newColliders.add(c);
				} else if (selecttype == 1) {
					LevelProp c = new LevelProp(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 1.001f, selectasset);
					newObjects.add(c);
				} else if (selecttype == 2) {
					ColorRect c = new ColorRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 1.001f, selectcolor);
					newObjects.add(c);
				}
				levelUpdate();
			}
		} else {
			Point arm = new Point(PLAYER_SCREEN_LOC.x + location.x + PLAYER_WIDTH / 2,
					PLAYER_SCREEN_LOC.y + location.y + PLAYER_SCREEN_LOC.height * Globals.ARM_VERTICAL_DISP);
			double angle = (Math.atan2(
					-pos.y + PLAYER_SCREEN_LOC.y + PLAYER_SCREEN_LOC.height * Globals.ARM_VERTICAL_DISP,
					-pos.x + PLAYER_SCREEN_LOC.x + PLAYER_WIDTH / 2)) % (2 * Math.PI) + Math.PI;
			Point start = new Point(arm.x + Globals.BULLET_DEFAULT_DISTANCE * Math.cos(angle),
					arm.y + Globals.BULLET_DEFAULT_DISTANCE * Math.sin(angle));
			Bullet b = new Bullet(start.x, start.y, angle);
			bullets.add(b);
		}
	}

	void keyEvents(boolean essentials, boolean typing, boolean game) {
		if (essentials) {
			if (keyPress(KeyEvent.VK_O) && gridsize_toggle) {
				gridsize_toggle = false;
				if (GRIDSIZE == Globals.GRIDSIZE) {
					GRIDSIZE = Globals.GRIDSIZE / Globals.DEBUG_SCALE;
					PLAYER_WIDTH = Globals.PLAYER_WIDTH / Globals.DEBUG_SCALE;
					PLAYER_HEIGHT = Globals.PLAYER_HEIGHT / Globals.DEBUG_SCALE;
					PLAYER_SPEED = Globals.DEBUG_PLAYER_SPEED;
					DEBUG_ = true;
					location = new Point(
							(location.x + PLAYER_SCREEN_LOC.getX() + PLAYER_SCREEN_LOC.getWidth() / 2)
									/ Globals.DEBUG_SCALE
									- PLAYER_SCREEN_LOC.getX() - PLAYER_SCREEN_LOC.getWidth() / 2,
							(location.y + PLAYER_SCREEN_LOC.getY() + PLAYER_SCREEN_LOC.getHeight() / 2)
									/ Globals.DEBUG_SCALE - PLAYER_SCREEN_LOC.getY()
									- PLAYER_SCREEN_LOC.getHeight() / 2);
				} else {
					GRIDSIZE = Globals.GRIDSIZE;
					PLAYER_WIDTH = Globals.PLAYER_WIDTH;
					PLAYER_HEIGHT = Globals.PLAYER_HEIGHT;
					PLAYER_SPEED = Globals.PLAYER_SPEED;
					DEBUG_ = false;
					location = new Point(
							(location.x + PLAYER_SCREEN_LOC.getX() + PLAYER_SCREEN_LOC.getWidth() / 2)
									* Globals.DEBUG_SCALE
									- PLAYER_SCREEN_LOC.getX() - PLAYER_SCREEN_LOC.getWidth() / 2,
							(location.y + PLAYER_SCREEN_LOC.getY() + PLAYER_SCREEN_LOC.getHeight() / 2)
									* Globals.DEBUG_SCALE - PLAYER_SCREEN_LOC.getY()
									- PLAYER_SCREEN_LOC.getHeight() / 2);
				}
				onResize(getWidth(), getHeight());
			}

			if (!keyPress(KeyEvent.VK_O))
				gridsize_toggle = true;
		}
		if (typing) {
			if (keyPress(KeyEvent.VK_ALT) && type_toggle) {
				type_toggle = false;
				typing = !typing;
				if (!typing)
					typing_str = "";
				entry.peripherals.typingEnable(typing);
			}
			if (!keyPress(KeyEvent.VK_ALT)) {
				type_toggle = true;
			}

			if (keyPress(KeyEvent.VK_ENTER) && enter_toggle) {
				enter_toggle = false;
				if (typing && typing_str.length() > 0) {
					typing = false;
					typing_str = typing_str.strip();
					if (selecttype == 1) {
						selectasset = typing_str;
					} else if (selecttype == 2) {
						selectcolor = typing_str;
					}
					typing_str = "";
					entry.peripherals.typingEnable(false);
				}
			}
			if (!keyPress(KeyEvent.VK_ENTER)) {
				enter_toggle = true;
			}
			if (keyPress(KeyEvent.VK_CONTROL) && backspace_toggle) {
				backspace_toggle = false;
				if (typing && typing_str.length() > 0) {
					typing_str = typing_str.substring(0, typing_str.length() - 1);
				}
			}
			if (!keyPress(KeyEvent.VK_CONTROL)) {
				backspace_toggle = true;
			}
		}
		if (game) {
			if (keyPress(KeyEvent.VK_X)) {
				selectstage = false;
			}

			// CHANGE SELECTION TYPE
			if (keyPress(KeyEvent.VK_Z) && selecttype_toggle) {
				selecttype_toggle = false;
				selecttype++;
				if (selecttype > 2)
					selecttype = 0;
			}
			if (!keyPress(KeyEvent.VK_Z)) {
				selecttype_toggle = true;
			}

			// Save level
			if (keyPress(KeyEvent.VK_P) && save_toggle) {
				save_toggle = false;
				LevelConfigUtil.saveLevel();
			}
			if (!keyPress(KeyEvent.VK_P)) {
				save_toggle = true;
			}

			// CLIP
			if (keyPress(KeyEvent.VK_C) && clip_toggle) {
				clip_toggle = false;
				CLIP = !CLIP;
				vertical_velocity = 0;
			}
			if (!keyPress(KeyEvent.VK_C)) {
				clip_toggle = true;
			}
		}
	}

	public void colliderCode() {
		component_x = 0;
		component_y = 0;

		int intent_x = 0;
		int intent_y = 0;
		double SPRINT = 1;
		if (keyPress(KeyEvent.VK_W))
			intent_y++;
		if (keyPress(KeyEvent.VK_S))
			intent_y--;
		if (keyPress(KeyEvent.VK_D))
			intent_x++;
		if (keyPress(KeyEvent.VK_A))
			intent_x--;
		if (keyPress(KeyEvent.VK_SHIFT) && sprint > 20)
			SPRINT = Globals.SPRINT_MULT;

		double speed = PLAYER_SPEED * SPRINT / Globals.REFRESH_RATE;
		if (!(intent_x == 0 && intent_y == 0)) {
			double angle = Math.atan2(intent_y, intent_x);
			if (CLIP) {
				component_x = speed * Math.cos(angle);
				component_y = speed * Math.sin(angle);
			} else {
				component_x = speed * intent_x;
			}
		}

		if (!CLIP) {
			vertical_velocity -= Globals.GRAV_CONST / Globals.REFRESH_RATE;
			component_y += vertical_velocity;

			grounded = false;
		}

		double max_disp = 0;

		for (Collider c : colliders) {
			Rect r = SchemUtilities.schemToLocal(c, location, GRIDSIZE);

			if (!CLIP) {
				// player collision
				CollisionReturn ret = CollisionUtil.DynamicCollision(PLAYER_SCREEN_LOC, r, component_x, component_y);
				if (ret.y_collision) {
					if (ret.intent_y == -1) {
						grounded = true;
						if (ret.disp_y > max_disp)
							max_disp = ret.disp_y;
					} else {
						vertical_velocity = 0;
					}
				}
				if (ret.x_collision) {
					component_x = 0;
				}
			}
			for (int i = 0; i < bullets.size(); i++) {
				Bullet b = bullets.get(i);
				Rect r2 = new Rect(b.x - location.x, b.y - location.y, b.width, b.height);
				double dx = Globals.BULLET_SPEED * Math.cos(b.getAngle());
				double dy = Globals.BULLET_SPEED * Math.sin(b.getAngle());
				CollisionReturn ret2 = CollisionUtil.DynamicCollision(r2, r, dx, -dy);
				double dist = Math
						.sqrt(Math.pow(b.x - PLAYER_SCREEN_LOC.x, 2) + Math.pow(b.y - PLAYER_SCREEN_LOC.y, 2));
				if (ret2.x_collision || ret2.y_collision || dist > Globals.BULLET_MAX_DISTANCE * GRIDSIZE) {
					bullets.remove(i);
					i--;
				} else {
					b.x += dx / Globals.REFRESH_RATE;
					b.y += dy / Globals.REFRESH_RATE;
				}
			}
		}

		if (grounded) {
			component_y = -max_disp;
			if(keyPress(KeyEvent.VK_H)){
				System.out.println(max_disp);
			}
			if (keyPress(KeyEvent.VK_W))
				vertical_velocity = Globals.JUMP_CONST * 10.0f / Globals.REFRESH_RATE;
			else
				vertical_velocity = 0;
		}
		if (keyPress(KeyEvent.VK_SHIFT)) {
			if (sprint > 0)
				sprint -= Globals.SPRINT_DRAIN * 10.0 / Globals.REFRESH_RATE;
			sprint_tick = entry.tick;
		} else {
			if (sprint_tick + Globals.SPRINT_DELAY < entry.tick && sprint != 100) {
				if (sprint + Globals.SPRINT_REGEN * 10.0 / Globals.REFRESH_RATE > 100)
					sprint = 100;
				else
					sprint += Globals.SPRINT_REGEN * 10.0 / Globals.REFRESH_RATE;
			}
		}

		location.x += component_x;
		location.y -= component_y;
	}

	// TICK EVENT
	public void onTick() {
		// show deathscreen for 3 seconds
		if (deathscreen) {
			if (entry.tick > deathscreen_tick + 3000) {
				deathscreen = false;
			}
			return;
		}
		// "pause" game while command is being typed
		if (typing) {
			if (entry.peripherals.keysTypedB()) {
				typing_str += entry.peripherals.keysTyped();
			}
			return;
		}

		keyEvents(true, typing, true);

		colliderCode();

		for (ResetBox b : resetboxes) {
			Rect r = SchemUtilities.schemToLocal(b, location, GRIDSIZE);
			boolean res = CollisionUtil.staticCollision(PLAYER_SCREEN_LOC, r);
			if (res) {
				vertical_velocity = 0;
				component_y = 0;
				deathscreen = true;
				deathscreen_tick = entry.tick;
				setPlayerPosFromSchem(checkpoints.get(b.checkpoint));
			}
		}
	}

	void levelUpdate() {
		TOPLEFT_BOUND = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
		BOTTOMRIGHT_BOUND = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE);
		for (Rect o : objects) {
			if (o.getX() < TOPLEFT_BOUND.x)
				TOPLEFT_BOUND.x = o.getX();
			if (o.getX() + o.getWidth() > BOTTOMRIGHT_BOUND.x)
				BOTTOMRIGHT_BOUND.x = o.getX() + o.getWidth();
			if (o.getY() < TOPLEFT_BOUND.y)
				TOPLEFT_BOUND.y = o.getY();
			if (o.getY() + o.getHeight() > BOTTOMRIGHT_BOUND.y)
				BOTTOMRIGHT_BOUND.y = o.getY() + o.getHeight();
		}
		for (Rect o : colliders) {
			if (o.getX() < TOPLEFT_BOUND.x)
				TOPLEFT_BOUND.x = o.getX();
			if (o.getX() + o.getWidth() > BOTTOMRIGHT_BOUND.x)
				BOTTOMRIGHT_BOUND.x = o.getX() + o.getWidth();
			if (o.getY() < TOPLEFT_BOUND.y)
				TOPLEFT_BOUND.y = o.getY();
			if (o.getY() + o.getHeight() > BOTTOMRIGHT_BOUND.y)
				BOTTOMRIGHT_BOUND.y = o.getY() + o.getHeight();
		}
		for (Rect o : newColliders) {
			if (o.getX() < TOPLEFT_BOUND.x)
				TOPLEFT_BOUND.x = o.getX();
			if (o.getX() + o.getWidth() > BOTTOMRIGHT_BOUND.x)
				BOTTOMRIGHT_BOUND.x = o.getX() + o.getWidth();
			if (o.getY() < TOPLEFT_BOUND.y)
				TOPLEFT_BOUND.y = o.getY();
			if (o.getY() + o.getHeight() > BOTTOMRIGHT_BOUND.y)
				BOTTOMRIGHT_BOUND.y = o.getY() + o.getHeight();
		}
		for (Rect o : newObjects) {
			if (o.getX() < TOPLEFT_BOUND.x)
				TOPLEFT_BOUND.x = o.getX();
			if (o.getX() + o.getWidth() > BOTTOMRIGHT_BOUND.x)
				BOTTOMRIGHT_BOUND.x = o.getX() + o.getWidth();
			if (o.getY() < TOPLEFT_BOUND.y)
				TOPLEFT_BOUND.y = o.getY();
			if (o.getY() + o.getHeight() > BOTTOMRIGHT_BOUND.y)
				BOTTOMRIGHT_BOUND.y = o.getY() + o.getHeight();
		}
	}

	boolean keyPress(int i) {
		return entry.peripherals.KeyPressed(i);
	}

	boolean inScreenSpace(Rect r) {
		return CollisionUtil.staticCollision(new Rect(0, 0, this.getWidth(), this.getHeight()), r);
	}

}
