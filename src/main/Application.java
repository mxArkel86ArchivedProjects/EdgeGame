package main;

import java.awt.image.BufferedImage;
import java.awt.Color;
import util.Rect;
import util.SchemUtilities;
import util.ScreenAnimation;
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

	/*
	* GAME OBJECTS
	*/
	public List<Collider> colliders = new ArrayList<Collider>();
	public List<GameObject> objects = new ArrayList<GameObject>();
	public List<ResetBox> resetboxes = new ArrayList<ResetBox>();
	public List<GameObject> newObjects = new ArrayList<GameObject>();
	public List<Collider> newColliders = new ArrayList<Collider>();
	public List<Bullet> bullets = new ArrayList<Bullet>();

	/*
	* GAME ASSETS
	*/
	public HashMap<String, Point> checkpoints = new HashMap<String, Point>();
	public HashMap<String, BufferedImage> assets = new HashMap<String, BufferedImage>();
	public HashMap<String, Color> colors = new HashMap<String, Color>();

	/*
	* GRAPHICS OBJECTS
	*/
	Font DEBUG_TEXT = new Font("Arial", Font.PLAIN, 12);
	Font UI_TEXT = new Font("Arial", Font.PLAIN, 28);

	/*
	* GAME CONSTANTS
	*/	
	double GRIDSIZE = Globals.GRIDSIZE;
	double PLAYER_WIDTH = Globals.PLAYER_WIDTH;
	double PLAYER_HEIGHT = Globals.PLAYER_HEIGHT;
	double PLAYER_SPEED = Globals.PLAYER_SPEED;
	boolean DEBUG_ = false;
	boolean CLIP = false;

	/*
	* PLAYER PARAMETERS
	*/
	int extra_jumps = 0;
	double vertical_velocity = 0;
	boolean grounded = false;
	int dash_count = 0;	
	int facing = 0;
	double sprint_val = 1;
	int intent_x = 0;
	int intent_y = 0;
	boolean sprint = false;
	
	/*
	* SELECTOR VARIABLES
	*/
	char selecttype = 0;
	String selectasset = "void";
	String selectcolor = "black";
	Point select_point_1 = new Point(0, 0);
	Point select_point_2 = new Point(0, 0);

	/*
	* MISC
	*/
	boolean deathscreen = false;
	boolean selectstage = false;
	boolean typing = false;
	String typing_str = "";
	
	/*
	* ANIMATION VARIABLES
	*/
	boolean animation = false;
	long animation_tick = 0;
	long animation_time = 3000;
	long sprint_tick = 0;
	long dash_tick = 0;

	/*
	* INIT METHOD
	*/
	public void Init(int width, int height) {
		
		onResize(width, height);

		
		ImageImport.ImportImageAssets();

		
		LevelConfigUtil.loadLevel();

		
		if (checkpoints.containsKey("start"))
			setPlayerPosFromSchem(checkpoints.get("start"));

		
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

		
		levelUpdate();
	}

	/*
	* BACKEND GAME CODE
	*/
	public void onTick() {
		inputUpdate(true, typing, true);

		deathscreen = ScreenAnimation.DeathScreen_Enabled(animation_tick);
		
		if(deathscreen)
			return;
		
		if (typing) {
			if (entry.peripherals.keysTypedB()) {
				typing_str += entry.peripherals.keysTyped();
			}
			return;
		}

		if (animation) {
			if (entry.tick - dash_tick < Globals.DASH_DURATION) {
				vertical_velocity = 0;
				double step_x = Globals.DASH_STEP / Globals.REFRESH_RATE*intent_x;
				double step_y = Globals.DASH_STEP / Globals.REFRESH_RATE*intent_y;
				CollisionReturn collided = playerCollision(step_x, step_y);
				if (collided == null) {
					location.x += step_x;
					location.y -= step_y;
				} else {
					location.x += collided.disp_x;
					location.y -= collided.disp_y;
					dash_tick = 0;
					animation = false;
				}
				return;
			}
			animation = false;
		} else {
			playerCollisionAndMovementCode();
		}

		for (ResetBox b : resetboxes) {
			Rect r = SchemUtilities.schemToLocal(b, location, GRIDSIZE);
			boolean res = CollisionUtil.staticCollision(PLAYER_SCREEN_LOC, r);
			if (res) {
				vertical_velocity = 0;
				deathscreen = true;
				animation_tick = entry.tick;
				setPlayerPosFromSchem(checkpoints.get(b.checkpoint));
			}
		}
	}

	/*
	* PAINT METHOD
	*/
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;

		
		Rect LEVEL_SCREEN_SPACE = new Rect(
				Math.max(0, (TOPLEFT_BOUND.x) * GRIDSIZE - location.x),
				Math.max(0, (TOPLEFT_BOUND.y) * GRIDSIZE - location.y),
				MathUtil.min_(BOTTOMRIGHT_BOUND.x * GRIDSIZE - location.x,
						getWidth() - TOPLEFT_BOUND.x * GRIDSIZE + location.x,
						(BOTTOMRIGHT_BOUND.x - TOPLEFT_BOUND.x) * GRIDSIZE, getWidth()),
				MathUtil.min_(BOTTOMRIGHT_BOUND.y * GRIDSIZE - location.y,
						getHeight() - TOPLEFT_BOUND.y * GRIDSIZE + location.y,
						(BOTTOMRIGHT_BOUND.y - TOPLEFT_BOUND.y) * GRIDSIZE, getHeight()));

		
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		
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

		
		
		

		
		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(1));

		if (DEBUG_) {
			for (int x1 = (int) TOPLEFT_BOUND.x; x1 < BOTTOMRIGHT_BOUND.x; x1++) {
				for (int y1 = (int) TOPLEFT_BOUND.y; y1 < BOTTOMRIGHT_BOUND.y; y1++) {
					Point p = new Point(x1 * GRIDSIZE - location.x, y1 * GRIDSIZE - location.y);

					Rect r1 = new Rect((int) p.x, (int) p.y, (int) (GRIDSIZE), (int) (GRIDSIZE));
					if (inScreenSpace(r1)) {
						g.drawLine((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.x),
								(int) Math.floor(p.y + GRIDSIZE));

						g.drawLine((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.x + GRIDSIZE),
								(int) Math.floor(p.y));
					}
				}
			}
		}

		
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4));
		for (Collider o : colliders) {
			Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
			if (inScreenSpace(r) && o.visible())
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		
		g.setColor(Color.ORANGE);
		for (Collider o : newColliders) {
			Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);

			if (inScreenSpace(r) && o.visible())
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));
		DrawUtil.DrawRect(g, PLAYER_SCREEN_LOC, Color.RED);

		
		for (Bullet b : bullets) {
			Rect r = new Rect(b.x - location.x, b.y - location.y, b.width, b.height);
			if (inScreenSpace(r))
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		
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

		
		g.setColor(Color.RED);
		g.fillRect(20, getHeight() - 60, 200, 10);
		g.setColor(Color.YELLOW);
		g.fillRect(20, getHeight() - 40, (int) (200 * sprint_val), 10);

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.BLACK);
		g.drawRect(20, getHeight() - 60, 200, 10);
		g.drawRect(20, getHeight() - 40, 200, 10);

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(2));

		
		if (DEBUG_) {
			for (ResetBox b : resetboxes) {
				Rect r = SchemUtilities.schemToLocal(b, location, GRIDSIZE);
				if (inScreenSpace(r))
					g.drawRect((int) Math.floor(r.getX()), (int) Math.floor(r.getY()), (int) Math.floor(r.getWidth()),
							(int) Math.floor(r.getHeight()));
			}

			g.setColor(Color.BLACK);
			Point mouse = entry.peripherals.mousePos();
			Point schem_mouse = SchemUtilities.schemPointFromFramePos(mouse, location, GRIDSIZE);
			int z = 3;
			g.fillOval((int) mouse.x - z, (int) mouse.y - z, 2 * z, 2 * z);

			g.setFont(DEBUG_TEXT);
			String focus = selectasset;
			if (selecttype == 2)
				focus = selectcolor;
			g.drawString(
					String.format(
							"raw=(%5.1f,%5.1f)  coord=(%5.1f,%5.1f) grounded=%b velocity=%3.1f stype=%d typing=%b focus=[%s]",
							mouse.x, mouse.y, schem_mouse.x, schem_mouse.y, grounded,
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
						SchemUtilities.schemPointFromFramePos(entry.peripherals.mousePos(), location, GRIDSIZE),
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

		
		if (typing) {
			g.setFont(UI_TEXT);
			g.setColor(Color.RED);
			g.drawString(typing_str, 40, 40);
		}

		
		if (deathscreen) {
			ScreenAnimation.DeathScreen_Graphics(g, animation_tick, this.getWidth(), this.getHeight());
		}
	}

	/*
	* RESIZE EVENT
	*/
	public void onResize(double width, double height) {
		PLAYER_SCREEN_LOC = new Rect((width - PLAYER_WIDTH) / 2, (height + 0.3 * height - PLAYER_HEIGHT) / 2,
				PLAYER_WIDTH,
				PLAYER_HEIGHT);
	}

	/*
	* MOUSE CLICK EVENT
	*/
	public void mouseClick(Point pos) {
		if (DEBUG_) {
			if (selectstage == false) {
				select_point_1 = SchemUtilities.schemPointFromFramePos(pos, location, GRIDSIZE);
				selectstage = true;
			} else if (selectstage == true) {
				select_point_2 = SchemUtilities.schemPointFromFramePos(pos, location, GRIDSIZE);
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

	/*
	* INPUT MANAGEMENT
	  Set local variables and toggles based on peripheral inputs
	*/
	void inputUpdate(boolean essentials, boolean typing, boolean game) {
		// Essential inputs that change global constants
		if (essentials) {
			if (entry.peripherals.KeyToggled(KeyEvent.VK_O)) {
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
		}
		// keys allowed while the console is up
		if (typing) {
			if (entry.peripherals.KeyToggled(KeyEvent.VK_ALT)) {
				typing = !typing;
				if (!typing)
					typing_str = "";
				entry.peripherals.typingEnable(typing);
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_ENTER)) {
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
			if (entry.peripherals.KeyPressed(KeyEvent.VK_CONTROL)) {
				if (typing && typing_str.length() > 0) {
					typing_str = typing_str.substring(0, typing_str.length() - 1);
				}
			}
		}
		// input while game is being played
		if (game) {
			int facing_ = (int) Math.copySign(1, entry.peripherals.mousePos().x - PLAYER_SCREEN_LOC.x);
			if(facing_!=0)
				facing = facing_;

			intent_x = 0;
			intent_y = 0;

			if (entry.peripherals.KeyPressed(KeyEvent.VK_W))
				intent_y++;
			if (entry.peripherals.KeyPressed(KeyEvent.VK_S))
				intent_y--;
			if (entry.peripherals.KeyPressed(KeyEvent.VK_D))
				intent_x++;
			if (entry.peripherals.KeyPressed(KeyEvent.VK_A))
				intent_x--;


			if (entry.peripherals.KeyToggled(KeyEvent.VK_X)) {
				selectstage = false;
			}

			
			if (entry.peripherals.KeyToggled(KeyEvent.VK_B)) {
				selecttype++;
				if (selecttype > 2)
					selecttype = 0;
			}

			
			if (entry.peripherals.KeyToggled(KeyEvent.VK_SHIFT)) {
				
				if (dash_count > 0) {
					dash_count--;
					dash_tick = entry.tick;
					animation = true;
				}
			}
			
			if (entry.peripherals.KeyPressed(KeyEvent.VK_Z)) {
				sprint = true;
				if (sprint_val > 0)
					sprint_val -= Globals.SPRINT_DRAIN / Globals.REFRESH_RATE;
				sprint_tick = entry.tick;
			} else {
				sprint = false;
				if (sprint_tick + Globals.SPRINT_DELAY < entry.tick && sprint_val != 1) {
					if (sprint_val + Globals.SPRINT_REGEN / Globals.REFRESH_RATE > 1)
						sprint_val = 1;
					else
						sprint_val += Globals.SPRINT_REGEN / Globals.REFRESH_RATE;
				}
			}

			
			if (entry.peripherals.KeyToggled(KeyEvent.VK_P)) {
				LevelConfigUtil.saveLevel();
			}

			
			if (entry.peripherals.KeyToggled(KeyEvent.VK_C)) {
				CLIP = !CLIP;
				vertical_velocity = 0;
			}
		}
	}

	/*
	* PLAYER COLLISION AND MOVEMENT CODE
	*/
	public void playerCollisionAndMovementCode() {
		double component_x = 0;
		double component_y = 0;

		double SPRINT = 1;
		if (sprint && sprint_val > 0.2)
			SPRINT = Globals.SPRINT_MULT;

		double speed = PLAYER_SPEED * SPRINT / Globals.REFRESH_RATE;
		if (!(intent_x == 0 && intent_y == 0)) {
			double angle = Math.atan2(intent_y, intent_x);
			if (CLIP) {
				component_x = speed * Math.cos(angle);
				component_y = speed * Math.sin(angle);
			} else {
				component_x = speed * intent_x;
				if(intent_y ==-1 && Math.abs(vertical_velocity)<Globals.DROP_SPEED/Globals.REFRESH_RATE){
					vertical_velocity = -Globals.DROP_SPEED/Globals.REFRESH_RATE;
				}
			}
		}

		if (!CLIP) {
			vertical_velocity -= Globals.GRAV_CONST / Globals.REFRESH_RATE;
			component_y += vertical_velocity;

			grounded = false;
		}

		double min_disp = Double.MAX_VALUE;

		for (Collider c : colliders) {
			Rect r = SchemUtilities.schemToLocal(c, location, GRIDSIZE);

			if (!CLIP) {
				
				CollisionReturn ret = CollisionUtil.DynamicCollision(PLAYER_SCREEN_LOC, r, component_x, component_y);
				if (ret.y_collision) {
					if (ret.intent_y == -1) {
						grounded = true;
						if (Math.abs(ret.disp_y) < Math.abs(min_disp))
							min_disp = ret.disp_y;
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
			if(min_disp!=Double.MAX_VALUE)
				component_y = min_disp;
			vertical_velocity = 0;
			extra_jumps = Globals.EXTRA_JUMPS;
			dash_count = Globals.DASH_COUNT;
		}
		if (entry.peripherals.KeyPressed(KeyEvent.VK_W)) {
			if (grounded || extra_jumps > 0) {
				if (!grounded)
					extra_jumps--;
				vertical_velocity = Globals.JUMP_CONST * 10.0f / Globals.REFRESH_RATE;
			}
		}

		location.x += component_x;
		location.y -= component_y;
	}

	/*
	* CHECKS PLAYER COLLISION WITH ALL OBJECTS IN THE SCENE
	*/
	public CollisionReturn playerCollision(double x, double y) {
		CollisionReturn ret1 = null;
		for (Collider c : colliders) {
			Rect r = SchemUtilities.schemToLocal(c, location, GRIDSIZE);

			CollisionReturn ret = CollisionUtil.DynamicCollision(PLAYER_SCREEN_LOC, r, x, y);
			if (ret.x_collision || ret.y_collision) {
				if (ret1 == null)
					ret1 = ret;
				else {
					if (ret.x_collision) {
						if (Math.abs(ret.disp_x) > Math.abs(ret1.disp_x))
							ret1.disp_x = ret.disp_x;
					}
					if (ret.y_collision) {
						if (Math.abs(ret.disp_y) > Math.abs(ret1.disp_y))
							ret1.disp_y = ret.disp_y;
					}
				}
			}

		}
		return ret1;
	}
	
		/*
	* UPDATE SCHEMATIC LEVEL BOUNDS BASED ON GAME OBJECTS
	*/
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

	/*
	* Move player given a SCHEMATIC coordinate
	*/
	void setPlayerPosFromSchem(Point p) {
		location = new Point(p.getX() * GRIDSIZE - PLAYER_SCREEN_LOC.getX(),
				p.getY() * GRIDSIZE - PLAYER_SCREEN_LOC.getY() - PLAYER_SCREEN_LOC.getHeight() / 2 - 1.5 * GRIDSIZE);
	}

	/*
	* CHECK IF RECTANGLE IS INSIDE SCREEN SPACE
	*/
	boolean inScreenSpace(Rect r) {
		return CollisionUtil.staticCollision(new Rect(0, 0, this.getWidth(), this.getHeight()), r);
	}

}
