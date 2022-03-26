package main;

import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.image.ColorModel;
import java.io.File;

import util.Rect;
import util.SchemUtilities;
import util.ScreenAnimation;
import util.Size;
import util.AssetSet;
import util.CollisionReturn;
import util.CollisionUtil;
import util.LevelConfigUtil;
import util.MathUtil;
import util.DrawUtil;
import util.ImageDuo;
import util.ImageImport;
import util.Point;
import java.awt.geom.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import java.awt.*;

import gameObjects.Bullet;
import gameObjects.Collider;
import gameObjects.ColorRect;
import gameObjects.GameObject;
import gameObjects.LevelProp;
import gameObjects.ResetBox;
import inventory.Gun;
import inventory.ItemAttributes;
import inventory.Magazine;
import inventory.Weapon;

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
	public HashMap<String, AssetSet> assets = new HashMap<>();
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
	int lastfacing = 1;
	double sprint_val = 1;
	int intent_x = 0;
	int intent_y = 0;
	boolean sprint = false;
	double looking_angle = 0;
	Weapon weapon = ItemAttributes.DevTek_Rifle();
	long last_fire_tick = 0;

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
	long walk_tick = 0;
	long dash_delay_tick = 0;
	int player_anim = 0;

	List<ImageDuo> anim_set = new ArrayList<>();

	GraphicsConfiguration gconfig = null;

	List<Double> debug_vals = Arrays.asList(0.0, 0.0, 0.0);
	int select_val = 0;

	/*
	 * INIT METHOD
	 */
	public void Init(int width, int height) {

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gconfig = ge.getDefaultScreenDevice().getDefaultConfiguration();

		onResize(width, height);


		ImageDuo duo1 = new ImageDuo();
		duo1.img1 = resizeToGrid(ImageImport.getImage("assets/playermodel_0.png"), PLAYER_WIDTH, PLAYER_HEIGHT);
		duo1.img2 = ImageImport.flippedImage(duo1.img1);
		anim_set.add(duo1);

		ImageDuo duo2 = new ImageDuo();
		duo2.img1 = resizeToGrid(ImageImport.getImage("assets/playermodel_1.png"), PLAYER_WIDTH, PLAYER_HEIGHT);
		duo2.img2 = ImageImport.flippedImage(duo2.img1);
		anim_set.add(duo2);

		
		LevelConfigUtil.loadLevel();

		HashMap<String, List<Size>> sizes = new HashMap<>();
		for (GameObject o : objects) {
			if (o instanceof LevelProp) {
				LevelProp lp = (LevelProp) o;
				List<Size> sizes_ = null;
				if (sizes.containsKey(lp.getAsset())) {
					sizes_ = sizes.get(lp.getAsset());
				} else {
					sizes_ = new ArrayList<Size>();
				}
				sizes_.add(new Size(lp.width, lp.height));
				sizes.put(lp.getAsset(), sizes_);
			}
		}

		for (final File fileEntry : new File("assets").listFiles()) {
			if (fileEntry.isFile()) {
				if (!fileEntry.getName().substring(fileEntry.getName().indexOf(".") + 1).equalsIgnoreCase("png")) {
					continue;
				}
				String name = fileEntry.getName().substring(0, fileEntry.getName().indexOf("."));
				BufferedImage img = ImageImport.getImage(fileEntry.getPath());

				AssetSet set = new AssetSet(img);
				if (sizes.containsKey(name)) {
					for (Size s : sizes.get(name)) {
						set.addAsset(s.width, s.height);
					}
				}
				assets.put(name, set);
			}
		}

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
		inputUpdate(true, typing, (!animation && !typing));
		if(entry.peripherals.mouse_state)
			mouseDown(entry.peripherals.mousePos());

		deathscreen = ScreenAnimation.DeathScreen_Enabled(animation_tick);

		if (deathscreen)
			return;

		if (typing) {
			if (entry.peripherals.keysTypedB()) {
				typing_str += entry.peripherals.keysTyped();
			}
			return;
		}

		if (animation) {
			double step_x = Globals.DASH_STEP * intent_x;
			double step_y = Globals.DASH_STEP * intent_y;
			if (entry.tick - dash_tick < Globals.DASH_DURATION) {
				vertical_velocity = 0;

				CollisionReturn collided = playerCollision(step_x, step_y);
				if (collided == null || CLIP) {
					location.x += step_x;
					location.y -= step_y;
				} else {
					if (collided.x_collision && collided.y_collision) {
						location.x += collided.disp_x;
						location.y -= collided.disp_y;
						dash_tick = 0;
						animation = false;
					} else if (collided.x_collision && !collided.y_collision) {
						location.x += collided.disp_x;
						location.y -= step_y;
					} else if (!collided.x_collision && collided.y_collision) {
						location.x += step_x;
						location.y -= collided.disp_y;
					}

				}

			} else {
				animation = false;
				vertical_velocity = step_y / Globals.DASH_DURATION * 1000 * Globals.DASH_PERCENT_FALLOFF_SPEED;
			}
		}

		playerCollisionAndMovementCode();
		
		

		if (grounded) {
			if (intent_x == 0) {
				player_anim = 0;
				walk_tick = entry.tick;
			} else {
				if (entry.tick - walk_tick > 400) {
					walk_tick = entry.tick;
					if (player_anim == 0)
						player_anim = 1;
					else
						player_anim = 0;
				}
			}
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

	void RawGame(Graphics2D g) {
		for (GameObject o : objects) {
			if (Math.ceil(o.getZ()) <= 0) {
				if (o instanceof LevelProp) {
					Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						paintLevelProp(g, (LevelProp) o);
				} else if (o instanceof ColorRect) {
					Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						paintColorRect(g, (ColorRect) o, 0);
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

		
		
		
		ImageDuo iduo = anim_set.get(player_anim);
		BufferedImage pimg = null;
		if (lastfacing == -1)
			pimg = iduo.img2;
		else if (lastfacing == 1)
			pimg = iduo.img1;
		g.drawImage(pimg, (int) PLAYER_SCREEN_LOC.x, (int) PLAYER_SCREEN_LOC.y, (int) PLAYER_SCREEN_LOC.width,
				(int) PLAYER_SCREEN_LOC.height, null);

		for (Bullet b : bullets) {
			Rect r = new Rect(b.x - location.x, b.y - location.y, b.width, b.height);
			if (inScreenSpace(r))
				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		}

		for (GameObject o : objects) {
			if (Math.ceil(o.getZ()) > 0) {
				if (o instanceof LevelProp) {
					Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
					if (inScreenSpace(r))
						paintLevelProp(g, (LevelProp) o);
				} else if (o instanceof ColorRect) {
					Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
					if (inScreenSpace(r))
						paintColorRect(g, (ColorRect) o, 0);
				}
			}
		}

		for (GameObject o : newObjects) {
			if (o instanceof ColorRect) {
				Rect r = SchemUtilities.schemToLocal(o, location, GRIDSIZE);
				if (inScreenSpace(r))
					paintColorRect(g, (ColorRect) o, o.getZ());
			} else if (o instanceof LevelProp) {
				Rect r = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
				if (inScreenSpace(r))
					paintLevelProp(g, (LevelProp) o);
			}
		}

	}

	private Shape LightMask(Graphics2D g) {
		double fov = 0.6;
		double dx = 360 * Math.cos(looking_angle);
		double dy = 360 * Math.sin(looking_angle);
		double STEP = Math.PI / 24;
		double dist = 520;
		double r = 200;
		double lamp_dist = 140;

		Area shape = new Area();
		Polygon circle = new Polygon();
		
		{
			Point center = new Point(PLAYER_SCREEN_LOC.x + PLAYER_SCREEN_LOC.width / 2,
					PLAYER_SCREEN_LOC.y + PLAYER_SCREEN_LOC.height / 2);
			double angle_1 = looking_angle - fov;
			double angle_2 = looking_angle + fov;
			circle.addPoint((int) (center.x + r * Math.cos(angle_1)),
					(int) (center.y + r * Math.sin(angle_1)));

			circle.addPoint((int) (center.x + dx + dist * Math.cos(angle_1)),
					(int) (center.y + dy + dist * Math.sin(angle_1)));
			circle.addPoint((int) (center.x + dx + dist * Math.cos(angle_2)),
					(int) (center.y + dy + dist * Math.sin(angle_2)));

			circle.addPoint((int) (center.x + r * Math.cos(angle_2)),
					(int) (center.y + r * Math.sin(angle_2)));

			for (double a = angle_2; a + STEP < angle_1 + 2 * Math.PI; a += STEP) {
				circle.addPoint((int) (center.x + r * Math.cos(a)),
						(int) (center.y + r * Math.sin(a)));
			}
			circle.addPoint((int) (center.x + r * Math.cos(angle_1)),
					(int) (center.y + r * Math.sin(angle_1)));
		}
		shape.add(new Area(circle));
		{
			for (GameObject o : objects) {

				if (o instanceof LevelProp) {
					LevelProp p = (LevelProp) o;
					if (p.getAsset().equals("lamp")) {
						Rect rect = SchemUtilities.schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ(), GRIDSIZE);
						if (inScreenSpace(rect.extend(Math.exp(7)))) {
							Shape ellipse = new Ellipse2D.Float((int) (rect.getX() - lamp_dist),
									(int) (rect.getY() - lamp_dist),
									(int) (rect.getWidth() + 2 * lamp_dist), (int) (rect.getWidth() + 2 * lamp_dist));
							shape.add(new Area(ellipse));
						}

					}
				}
			}
		}

		return shape;
	}

	void drawUI(Graphics2D g) {
		for (ResetBox b : resetboxes) {
			Rect r = SchemUtilities.schemToLocal(b, location, GRIDSIZE);
			if (inScreenSpace(r))
				g.drawRect((int) Math.floor(r.getX()), (int) Math.floor(r.getY()), (int) Math.floor(r.getWidth()),
						(int) Math.floor(r.getHeight()));
		}

		g.setColor(Color.WHITE);
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
						"raw=(%5.1f,%5.1f)  coord=(%5.1f,%5.1f) grounded=%b velocity=%3.1f stype=%d typing=%b focus=[%s] select_val=%d debug_val=[%s]",
						mouse.x, mouse.y, schem_mouse.x, schem_mouse.y, grounded,
						vertical_velocity, (int) selecttype, typing, focus, select_val, debug_vals.toString()),
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
					g.drawImage(assets.get(selectasset).getBaseAsset(), (int) r.getX(), (int) r.getY(),
							(int) r.getWidth(),
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

		if (typing) {
			g.setFont(UI_TEXT);
			g.setColor(Color.RED);
			g.drawString(typing_str, 40, 40);
		}

		if (deathscreen) {
			ScreenAnimation.DeathScreen_Graphics(g, animation_tick, this.getWidth(), this.getHeight());
		}

		g.setColor(Color.RED);
		g.fillRect(20, getHeight() - 60, 200, 10);
		g.setColor(new Color(255, (int) (100 + (120) * sprint_val), 0));
		g.fillRect(20, getHeight() - 40, (int) (200 * sprint_val), 10);

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.BLACK);
		g.drawRect(20, getHeight() - 60, 200, 10);
		g.drawRect(20, getHeight() - 40, 200, 10);

		g.setColor(Color.WHITE);
		g.setFont(UI_TEXT);

		Gun gun = (Gun) weapon;
		if (gun.mag != null) {
			String bullet_str = String.format("%d/%d", gun.mag.bullet_count, gun.mag.BULLET_MAX());
			g.drawString(bullet_str,
					(int) (-g.getFontMetrics().getStringBounds(bullet_str, g).getWidth() + this.getWidth() - 20),
					(int) (-g.getFontMetrics().getAscent() - 20 + this.getHeight()));
		}

		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2));
		Point mousepos = entry.peripherals.mousePos();
		if (entry.tick - last_fire_tick < gun.FIRING_DELAY()) {
			int size1 = 14;
			double percent = ((entry.tick - last_fire_tick) * 1.0f / gun.FIRING_DELAY());
			g.drawArc((int) mousepos.x - size1, (int) mousepos.y - size1, 2 * size1, 2 * size1, 0,
					(int) (360 * percent));
		}
	}
	/*
	 * PAINT METHOD
	 */

	Rect LEVEL_SCREEN_SPACE;
	VolatileImage raw_game = null;
	VolatileImage light_mask = null;
	BufferedImage display = null;

	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		Graphics2D g_raw = (Graphics2D) raw_game.getGraphics();
		Graphics2D g_light = (Graphics2D) light_mask.getGraphics();
		Graphics2D g_display = (Graphics2D) display.getGraphics();

		
		g_raw.setBackground(new Color(0, 0, 0));
		g_raw.clearRect(0, 0, raw_game.getWidth(), raw_game.getHeight());

		LEVEL_SCREEN_SPACE = new Rect(
				Math.max(0, (TOPLEFT_BOUND.x) * GRIDSIZE - location.x),
				Math.max(0, (TOPLEFT_BOUND.y) * GRIDSIZE - location.y),
				MathUtil.min_(BOTTOMRIGHT_BOUND.x * GRIDSIZE - location.x,
						getWidth() - TOPLEFT_BOUND.x * GRIDSIZE + location.x,
						(BOTTOMRIGHT_BOUND.x - TOPLEFT_BOUND.x) * GRIDSIZE, getWidth()),
				MathUtil.min_(BOTTOMRIGHT_BOUND.y * GRIDSIZE - location.y,
						getHeight() - TOPLEFT_BOUND.y * GRIDSIZE + location.y,
						(BOTTOMRIGHT_BOUND.y - TOPLEFT_BOUND.y) * GRIDSIZE, getHeight()));

		if (!DEBUG_)
			g_raw.setClip(LightMask(g_raw));
		else
			g_raw.setClip(0, 0, raw_game.getWidth(), raw_game.getHeight());
		RawGame(g_raw);
		
		g.drawImage(raw_game, 0, 0, null);
		drawUI(g);
	}

	/*
	 * RESIZE EVENT
	 */
	public void onResize(double width, double height) {
		PLAYER_SCREEN_LOC = new Rect((width - PLAYER_WIDTH) / 2, (height + 0.0 * height - PLAYER_HEIGHT) / 2,
				PLAYER_WIDTH,
				PLAYER_HEIGHT);
		
		
		
		

		display = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

		ImageCapabilities ic = new ImageCapabilities(true);
		try {
			raw_game = gconfig.createCompatibleVolatileImage((int) width, (int) height, ic, Transparency.TRANSLUCENT);
			light_mask = gconfig.createCompatibleVolatileImage((int) width, (int) height, ic, Transparency.TRANSLUCENT);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}


	public void mouseDown(Point pos) {
		Gun g = (Gun) weapon;
			if (g.FIRING_TYPE()==1 && g.mag != null) {
				if (entry.tick - last_fire_tick > g.FIRING_DELAY()) {
					FireBullet();
				}
			}
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
			Gun g = (Gun) weapon;
			if (g.FIRING_TYPE()==0 && g.mag != null) {
				if (entry.tick - last_fire_tick > g.FIRING_DELAY()) {
					FireBullet();
				}
			}

		}
	}

	void FireBullet() {
		Gun g = (Gun) weapon;
		Point pos = entry.peripherals.mousePos();

		if (g.MAG_TYPE() != g.mag.NAME()) {
			System.out.println("WRONG MAG INSERTED");
		}

		Point arm = new Point(PLAYER_SCREEN_LOC.x + location.x + PLAYER_WIDTH / 2,
				PLAYER_SCREEN_LOC.y + location.y + PLAYER_SCREEN_LOC.height * Globals.ARM_VERTICAL_DISP);
		double angle = (Math.atan2(
				-pos.y + PLAYER_SCREEN_LOC.y + PLAYER_SCREEN_LOC.height * Globals.ARM_VERTICAL_DISP,
				-pos.x + PLAYER_SCREEN_LOC.x + PLAYER_WIDTH / 2)) % (2 * Math.PI) + Math.PI;
		Point start = new Point(arm.x + Globals.BULLET_DEFAULT_DISTANCE * Math.cos(angle),
				arm.y + Globals.BULLET_DEFAULT_DISTANCE * Math.sin(angle));
		Bullet b = new Bullet(start.x, start.y, g.BULLET_SIZE(), angle, g.mag.BULLET_INITIAL_SPEED());
		bullets.add(b);

		last_fire_tick = entry.tick;
		g.mag.bullet_count--;
		if (g.mag.bullet_count == 0)
			g.mag = null;
	}
	/*
	 * INPUT MANAGEMENT
	 * Set local variables and toggles based on peripheral inputs
	 */
	void inputUpdate(boolean essentials, boolean typing, boolean game) {
		
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
			if (entry.peripherals.KeyToggled(KeyEvent.VK_I)) {
				if (select_val == debug_vals.size() - 1)
					select_val = 0;
				else
					select_val++;
			}
			if (entry.peripherals.KeyPressed(KeyEvent.VK_L)) {
				double val = debug_vals.get(select_val);
				debug_vals.set(select_val, val + 0.04);
			} else if (entry.peripherals.KeyPressed(KeyEvent.VK_K)) {
				double val = debug_vals.get(select_val);
				debug_vals.set(select_val, val - 0.04);
			}
		}
		
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
		
		if (game) {
			int facing_ = (int) Math.copySign(1, entry.peripherals.mousePos().x - PLAYER_SCREEN_LOC.x);
			if (facing_ != 0)
				facing = facing_;

			looking_angle = (Math.atan2(
					-entry.peripherals.mousePos().y + PLAYER_SCREEN_LOC.y
							+ PLAYER_SCREEN_LOC.height * Globals.ARM_VERTICAL_DISP,
					-entry.peripherals.mousePos().x + PLAYER_SCREEN_LOC.x + PLAYER_WIDTH / 2)) % (2 * Math.PI)
					+ Math.PI;

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

			if (intent_x != 0)
				lastfacing = intent_x;

			if (entry.peripherals.KeyToggled(KeyEvent.VK_X)) {
				selectstage = false;
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_B)) {
				selecttype++;
				if (selecttype > 2)
					selecttype = 0;
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_SHIFT)) {

				if (dash_count > 0 || CLIP && entry.tick - dash_delay_tick > Globals.DASH_DELAY) {
					dash_delay_tick = entry.tick;
					dash_count--;
					dash_tick = entry.tick;
					animation = true;
				}
			}

			if (entry.peripherals.KeyPressed(KeyEvent.VK_Z)) {
				sprint = true;
				if (sprint_val > 0)
					sprint_val -= Globals.SPRINT_DRAIN;
				sprint_tick = entry.tick;
			} else {
				sprint = false;
				if (sprint_tick + Globals.SPRINT_DELAY < entry.tick && sprint_val != 1) {
					if (sprint_val + Globals.SPRINT_REGEN > 1)
						sprint_val = 1;
					else
						sprint_val += Globals.SPRINT_REGEN;
				}
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_P)) {
				LevelConfigUtil.saveLevel();
				levelUpdate();
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_C)) {
				CLIP = !CLIP;
				vertical_velocity = 0;
			}

			if (entry.peripherals.KeyToggled(KeyEvent.VK_U)) {
				((Gun) weapon).mag = ItemAttributes.DevTek_Mag();
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
		if (sprint && sprint_val > 0.0)
			SPRINT = Globals.SPRINT_MULT;

		double speed = PLAYER_SPEED * SPRINT;
		if (!(intent_x == 0 && intent_y == 0)) {
			double angle = Math.atan2(intent_y, intent_x);
			if (CLIP) {
				component_x = speed * Math.cos(angle);
				component_y = speed * Math.sin(angle);
			} else {
				component_x = speed * intent_x;
				if (intent_y == -1 && Math.abs(vertical_velocity) < Globals.DROP_SPEED) {
					vertical_velocity = -Globals.DROP_SPEED;
				}
			}
		}

		if (!CLIP) {
			vertical_velocity -= Globals.GRAV_CONST;
			component_y += vertical_velocity;

			grounded = false;
		}

		double min_disp = Double.MAX_VALUE;

		if (!CLIP) {
			for (Collider c : colliders) {
				Rect r = SchemUtilities.schemToLocal(c, location, GRIDSIZE);

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
		}

		for (Collider c : colliders) {
			Rect collider = SchemUtilities.schemToLocal(c, location, GRIDSIZE);

			for (int i = 0; i < bullets.size(); i++) {
				Bullet b = bullets.get(i);
				Rect bullet = new Rect(b.x - location.x, b.y - location.y, b.width, b.height);
				double dx = b.speed * Math.cos(b.angle);
				double dy = b.speed * Math.sin(b.angle);

				double speed_x = dx;
				double speed_y = dy + Globals.BULLET_GRAV_CONST;
				double new_angle = Math.atan2(speed_y, speed_x);
				double new_speed = Math.sqrt(Math.pow(speed_x, 2) + Math.pow(speed_y, 2));
				b.speed = new_speed;
				b.angle = new_angle;

				boolean ret = CollisionUtil.staticCollision(bullet, collider);

				if (ret || Math.sqrt(Math.pow(bullet.x, 2) + Math.pow(bullet.y, 2)) > 4000) {
					bullets.remove(i);
					i--;
				} else {
					b.x += dx;
					b.y += dy;
				}
			}

		}

		if (grounded) {
			if (min_disp != Double.MAX_VALUE)
				component_y = min_disp;
			vertical_velocity = 0;
			extra_jumps = Globals.EXTRA_JUMPS;
			dash_count = Globals.DASH_COUNT;
		}
		if (entry.peripherals.KeyToggled(KeyEvent.VK_W)) {
			if (grounded || extra_jumps > 0) {
				if (!grounded)
					extra_jumps--;
				vertical_velocity = Globals.JUMP_CONST * 10.0f;
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
						ret1.x_collision = true;
						if (Math.abs(ret.disp_x) > Math.abs(ret1.disp_x))
							ret1.disp_x = ret.disp_x;
					}
					if (ret.y_collision) {
						ret1.y_collision = true;
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

	void paintColorRect(Graphics g, ColorRect rect, double depth) {
		Rect r = SchemUtilities.schemToLocal(rect, location, GRIDSIZE);
		float c = 0.05f;
		if (!entry.app.colors.containsKey(rect.getColor())) {
			g.setColor(Color.RED);
			g.fillRect((int) Math.floor(r.getX() - c), (int) Math.floor(r.getY() - c),
					(int) Math.ceil(r.getWidth() + c), (int) Math.ceil(r.getHeight() + c));
		} else {
			g.setColor(entry.app.colors.get(rect.getColor()));
			g.fillRect((int) Math.floor(r.getX() - c), (int) Math.floor(r.getY() - c),
					(int) Math.ceil(r.getWidth() + c), (int) Math.ceil(r.getHeight() + c));
		}
	}

	void paintLevelProp(Graphics g, LevelProp p) {
		Rect r = SchemUtilities.schemToLocalZ(p, PLAYER_SCREEN_LOC, location, p.getZ(), GRIDSIZE);
		if (!entry.app.assets.containsKey(p.getAsset())) {
			g.setColor(Color.RED);
			g.fillRect((int) Math.round(r.getX()), (int) Math.round(r.getY()), (int) r.getWidth(), (int) r.getHeight());
		} else {
			AssetSet as1 = entry.app.assets.get(p.getAsset());
			BufferedImage img = as1.getAsset(p.width, p.height);
			g.drawImage(img, (int) Math.round(r.getX()), (int) Math.round(r.getY()), (int) r.getWidth(),
					(int) r.getHeight(), null);
		}
	}

	BufferedImage resizeToGrid(BufferedImage img, double width, double height) {
		return ImageImport.resize(img, (int) (width / GRIDSIZE * Globals.PIXELS_PER_GRID),
				(int) (height / GRIDSIZE * Globals.PIXELS_PER_GRID));
	}

}