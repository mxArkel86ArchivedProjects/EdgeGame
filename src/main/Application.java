package main;

import java.awt.image.BufferedImage;
import java.awt.Color;
import util.Rect;
import util.CollisionReturn;
import util.CollisionUtil;
import util.ConfigImport;
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

import gameObjects.Collider;
import gameObjects.ColorRect;
import gameObjects.GameObject;
import gameObjects.LevelProp;
import gameObjects.ResetBox;

public class Application extends JPanel {
	Rect PLAYER_SCREEN_LOC = null;
	// BOTTOMRIGHT_BOUND = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
	Point TOPLEFT_BOUND = new Point(0, 0);
	Point BOTTOMRIGHT_BOUND = new Point(0, 0);
	Point location = new Point(0, 0);

	public List<Collider> colliders = new ArrayList<Collider>();
	public List<GameObject> objects = new ArrayList<GameObject>();
	public List<ResetBox> resetboxes = new ArrayList<ResetBox>();

	public HashMap<String, Point> checkpoints = new HashMap<String, Point>();
	public HashMap<String, BufferedImage> assets = new HashMap<String, BufferedImage>();
	public HashMap<String, Color> colors = new HashMap<String, Color>();

	Font DEATHSCREEN_TEXT = new Font("Arial", Font.BOLD, 48);
	Font DEBUG_TEXT = new Font("Arial", Font.PLAIN, 12);

	double vertical_velocity = 0;
	boolean grounded = false;
	int GRIDSIZE = Globals.GRIDSIZE;
	int PLAYERSIZE = Globals.PLAYER_SIZE;
	boolean gridsize_toggle = true;
	double PLAYER_SPEED = Globals.PLAYER_SPEED;
	boolean DEBUG_ = false;
	double component_x = 0;
	double component_y = 0;
	boolean enabled = false;
	boolean deathscreen = false;
	long deathscreen_tick = 0;

	public void Init(int width, int height) {
		onResize(width, height);
		ImageImport.ImportImageAssets();

		ConfigImport.loadLevel();

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

	void setPlayerPosFromSchem(Point p) {
		location = new Point(p.getX() * GRIDSIZE - PLAYER_SCREEN_LOC.getX(),
				p.getY() * GRIDSIZE - PLAYER_SCREEN_LOC.getY() - PLAYER_SCREEN_LOC.getHeight() / 2 - 1.5 * GRIDSIZE);
	}

	Point schemPointFromMouse() {
		Point p = entry.peripherals.mousePos();
		return new Point(Math.round((p.getX() + location.getX()) / GRIDSIZE),
				Math.round((p.getY() + location.getY()) / GRIDSIZE));
	}

	double min_(double... mins) {
		double val = mins[0];
		for (double d : mins) {
			val = Math.min(val, d);
		}
		return val;
	}

	double max_(double... maxs) {
		double val = maxs[0];
		for (double d : maxs) {
			val = Math.min(val, d);
		}
		return val;
	}

	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		
		Rect LEVEL_SCREEN_SPACE = new Rect(
				Math.max(0, (TOPLEFT_BOUND.x) * GRIDSIZE - location.x),
				Math.max(0, (TOPLEFT_BOUND.y) * GRIDSIZE - location.y),
				min_(BOTTOMRIGHT_BOUND.x * GRIDSIZE - location.x, getWidth() - TOPLEFT_BOUND.x * GRIDSIZE + location.x,
						(BOTTOMRIGHT_BOUND.x - TOPLEFT_BOUND.x) * GRIDSIZE, getWidth()),
				min_(BOTTOMRIGHT_BOUND.y * GRIDSIZE - location.y, getHeight() - TOPLEFT_BOUND.y * GRIDSIZE + location.y,
						(BOTTOMRIGHT_BOUND.y - TOPLEFT_BOUND.y) * GRIDSIZE, getHeight()));

		// clear canvas
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// draw ColorRect and LevelProps
		for (GameObject o : objects) {
			if (o.getZ() <= 1) {
				if (o instanceof ColorRect) {
					Rect r = schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				} else if (o instanceof LevelProp) {
					Rect r = schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				}
			}
		}

		g.setColor(new Color(30, 30, 30, 160));
		g.fillRect((int) LEVEL_SCREEN_SPACE.getX(), (int) LEVEL_SCREEN_SPACE.getY(),
				(int) LEVEL_SCREEN_SPACE.getWidth(), (int) LEVEL_SCREEN_SPACE.getHeight());

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

		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4));
		for (Collider o : colliders) {
			Rect r = schemToLocal(o, location, GRIDSIZE);
			if (inScreenSpace(r) && o.visible())
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));

		DrawUtil.DrawRect(g, PLAYER_SCREEN_LOC, Color.RED);

		for (GameObject o : objects) {
			if (o.getZ() > 1) {
				if (o instanceof ColorRect) {
					Rect r = schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				} else if (o instanceof LevelProp) {
					Rect r = schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						o.paint(g, r);
				}
			}
		}

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(2));

		if(DEBUG_){
		for (ResetBox b : resetboxes) {
			Rect r = schemToLocal(b, location, GRIDSIZE);
			if (inScreenSpace(r))
				g.drawRect((int) Math.floor(r.getX()), (int) Math.floor(r.getY()), (int) Math.floor(r.getWidth()),
						(int) Math.floor(r.getHeight()));
		}

		
		g.setColor(Color.ORANGE);
		Point mouse = entry.peripherals.mousePos();
		Point schem_mouse = schemPointFromMouse();
		int z = 3;
		g.fillOval((int) mouse.x - z, (int) mouse.y - z, 2 * z, 2 * z);
		

		g.setFont(DEBUG_TEXT);
		g.drawString(
				String.format("raw=(%.1f,%.1f)  coord=(%.1f,%.1f) grounded=%b component=(%.1f,%.1f) velocity=%.1f", mouse.x, mouse.y, schem_mouse.x, schem_mouse.y, grounded, component_x, component_y, vertical_velocity), 20,
				g.getFontMetrics().getAscent() + 20);

		g.setStroke(new BasicStroke(6));
		g.setColor(Color.BLUE);
		g.drawRect((int) LEVEL_SCREEN_SPACE.getX(), (int) LEVEL_SCREEN_SPACE.getY(),
				(int) LEVEL_SCREEN_SPACE.getWidth(), (int) LEVEL_SCREEN_SPACE.getHeight());
			}

		if(deathscreen){
					int a = 255;
					if(entry.tick-deathscreen_tick>2000)
						a = 255-(int)Math.min(255,(entry.tick-deathscreen_tick-2000)*255/1000);
					//System.out.println("out=" + a);
					g.setFont(DEATHSCREEN_TEXT);
					g.setColor(new Color(0,0,0,a));
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					g.setStroke(new BasicStroke(2));
		
					g.setColor(new Color(255,255,255,a));
					String str = "Dead";
					Rectangle2D r2d = g.getFontMetrics().getStringBounds(str, 0, str.length(), g);
					g.drawString(str, (int)((getWidth()-r2d.getWidth())/2),  (int)((getHeight()+ g.getFontMetrics().getAscent())/2));
				
		}
	}

	static Rect schemToLocalZ(Rect r, Rect PLAYER_SCREEN_LOC, Point location, double z, double GRIDSIZE) {
		Point p = new Point(
				r.getX() * GRIDSIZE * z - location.x * z - (PLAYER_SCREEN_LOC.getX() * z - PLAYER_SCREEN_LOC.getX())
						+ (r.getWidth() * GRIDSIZE / 2 * z - r.getWidth() * GRIDSIZE / 2)
						- (PLAYER_SCREEN_LOC.getWidth() / 2 * z - PLAYER_SCREEN_LOC.getWidth() / 2),
				r.getY() * GRIDSIZE * z - location.y * z - PLAYER_SCREEN_LOC.getY() * z + PLAYER_SCREEN_LOC.getY()
						+ r.getHeight() * GRIDSIZE * z - r.getHeight() * GRIDSIZE - PLAYER_SCREEN_LOC.getHeight() * z
						+ PLAYER_SCREEN_LOC.getHeight());
		return new Rect(p.x, p.y, r.getWidth() * GRIDSIZE, r.getHeight() * GRIDSIZE);
	}

	static Rect schemToLocal(Rect r, Point location, double GRIDSIZE) {
		/// multiply by GRIDSIZE, subtract camera location
		return new Rect(r.getX() * GRIDSIZE - location.x, r.getY() * GRIDSIZE - location.y, r.getWidth() * GRIDSIZE,
				r.getHeight() * GRIDSIZE);
	}

	public void onResize(double width, double height) {
		PLAYER_SCREEN_LOC = new Rect((width - PLAYERSIZE) / 2, (height - PLAYERSIZE) / 2, PLAYERSIZE, PLAYERSIZE);
	}

	public void onTick() {
		if(deathscreen){
			if(entry.tick>deathscreen_tick+3000){
				deathscreen = false;
			}
			return;
		}

		if (keyPress(KeyEvent.VK_O) && gridsize_toggle) {
			gridsize_toggle = false;
			if (GRIDSIZE == Globals.GRIDSIZE) {
				GRIDSIZE = Globals.GRIDSIZE / Globals.DEBUG_SCALE;
				PLAYERSIZE = Globals.PLAYER_SIZE / Globals.DEBUG_SCALE;
				PLAYER_SPEED = Globals.DEBUG_PLAYER_SPEED;
				DEBUG_ = true;
				location = new Point(
						(location.x + PLAYER_SCREEN_LOC.getX() + PLAYER_SCREEN_LOC.getWidth() / 2) / Globals.DEBUG_SCALE
								- PLAYER_SCREEN_LOC.getX() - PLAYER_SCREEN_LOC.getWidth() / 2,
						(location.y + PLAYER_SCREEN_LOC.getY() + PLAYER_SCREEN_LOC.getHeight() / 2)
								/ Globals.DEBUG_SCALE - PLAYER_SCREEN_LOC.getY() - PLAYER_SCREEN_LOC.getHeight() / 2);
			} else {
				GRIDSIZE = Globals.GRIDSIZE;
				PLAYERSIZE = Globals.PLAYER_SIZE;
				PLAYER_SPEED = Globals.PLAYER_SPEED;
				DEBUG_ = false;
				location = new Point(
						(location.x + PLAYER_SCREEN_LOC.getX() + PLAYER_SCREEN_LOC.getWidth() / 2) * Globals.DEBUG_SCALE
								- PLAYER_SCREEN_LOC.getX() - PLAYER_SCREEN_LOC.getWidth() / 2,
						(location.y + PLAYER_SCREEN_LOC.getY() + PLAYER_SCREEN_LOC.getHeight() / 2)
								* Globals.DEBUG_SCALE - PLAYER_SCREEN_LOC.getY() - PLAYER_SCREEN_LOC.getHeight() / 2);
			}
			onResize(getWidth(), getHeight());
		}
		
		if (!keyPress(KeyEvent.VK_O))
			gridsize_toggle = true;

			component_x = 0;
			component_y = 0;

		int intent_x = 0;
		int intent_y = 0;
		if (keyPress(KeyEvent.VK_W))
			intent_y++;
		if (keyPress(KeyEvent.VK_S))
			intent_y--;
		if (keyPress(KeyEvent.VK_D))
			intent_x++;
		if (keyPress(KeyEvent.VK_A))
			intent_x--;

		if (!(intent_x == 0 && intent_y == 0)) {
			double angle = Math.atan2(intent_y, intent_x);
			if (Globals.CLIP){
			component_x = PLAYER_SPEED / Globals.REFRESH_RATE * Math.cos(angle);
			component_y = PLAYER_SPEED / Globals.REFRESH_RATE * Math.sin(angle);
			}else{
				component_x = PLAYER_SPEED*intent_x/Globals.REFRESH_RATE;
			}
		}

		if (!Globals.CLIP) {
			vertical_velocity -= Globals.GRAV_CONST / Globals.REFRESH_RATE;
			component_y += vertical_velocity;

			grounded = false;

			double max_disp = 0;
			
			for(Collider c : colliders){
				Rect r = schemToLocal(c, location, GRIDSIZE);
				CollisionReturn ret = CollisionUtil.DynamicCollision(PLAYER_SCREEN_LOC, r, component_x, component_y);
				if(ret.y_collision){
					if(ret.intent_y==-1){
						grounded = true;
					if(ret.disp_y>max_disp)
					max_disp = ret.disp_y;
					}else{
						vertical_velocity = 0;
					}
				}
				if(ret.x_collision){
					component_x = 0;
				}
			}

			if(grounded){
				component_y = max_disp;
				if(keyPress(KeyEvent.VK_W))
					vertical_velocity = Globals.JUMP_CONST;
				else
				vertical_velocity = 0;
			}
			
		}

		for (ResetBox b : resetboxes) {
			Rect r = schemToLocal(b, location, GRIDSIZE);
			boolean res = CollisionUtil.staticCollision(PLAYER_SCREEN_LOC, r);
			if (res) {
				vertical_velocity = 0;
				component_y = 0;
				deathscreen = true;
				deathscreen_tick = entry.tick;
				setPlayerPosFromSchem(checkpoints.get(b.checkpoint));
			}
		}

		location.x += component_x;
		location.y -= component_y;
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
	}

	boolean keyPress(int i) {
		return entry.peripherals.KeyPressed(i);
	}

	boolean inScreenSpace(Rect r) {
		return CollisionUtil.staticCollision(new Rect(0, 0, this.getWidth(), this.getHeight()), r);
	}

}
