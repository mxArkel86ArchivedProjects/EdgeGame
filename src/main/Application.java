package main;

import java.awt.image.BufferedImage;
import java.awt.Color;
import util.Rect;
import util.CollisionUtil;
import util.ConfigImport;
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
import java.awt.BasicStroke;

import gameObjects.Collider;
import gameObjects.ColorRect;
import gameObjects.GameObject;
import gameObjects.LevelProp;

public class Application extends JPanel {
	Rect PLAYER_SCREEN_LOC = null;
	// 	BOTTOMRIGHT_BOUND = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
	Point TOPLEFT_BOUND = new Point(0, 0);
	Point BOTTOMRIGHT_BOUND = new Point(0, 0);
	Point location = new Point(0,0);
	
	public List<Collider> colliders = new ArrayList<Collider>();
	public List<GameObject> objects = new ArrayList<GameObject>();
	public HashMap<String, BufferedImage> assets = new HashMap<String, BufferedImage>();
	public HashMap<String, Color> colors = new HashMap<String, Color>();
	
	public void Init(){
		ImageImport.ImportImageAssets();
		
		colors.put("blue", Color.BLUE);
		
		objects.add(new ColorRect(1,1,2,2, 2, "blue"));
		//objects.add(new LevelProp(4,4,3,2, 1, "void"));
		

		ConfigImport.loadLevel();
		
		Collections.sort(objects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject a, GameObject b) {
                if(a.getZ()>b.getZ())
					return 1;
				else if(a.getZ()==b.getZ())
					return 0;
				else return -1;
            }
        });
		levelUpdated();
		
	}
	
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		int buff=1;

		Rect LEVEL_SCREEN_SPACE = new Rect(Math.max(0,(TOPLEFT_BOUND.x-buff)*Globals.GRIDSIZE-location.x), Math.max(0, (TOPLEFT_BOUND.y-buff)*Globals.GRIDSIZE-location.y), Math.min(this.getWidth(), (BOTTOMRIGHT_BOUND.x-TOPLEFT_BOUND.x+2*buff)*Globals.GRIDSIZE-location.x), Math.min(this.getHeight(), (BOTTOMRIGHT_BOUND.y-TOPLEFT_BOUND.y+2*buff)*Globals.GRIDSIZE-location.y));
		//clear canvas
		g.setColor(Color.WHITE);
		g.fillRect(0,0,this.getWidth(),this.getHeight());

		//draw ColorRect and LevelProps
		for(GameObject o : objects) {
			if(o.getZ()<=1){
			if(o instanceof ColorRect){
				Rect r = schemToLocal(o, location);
				if(inScreenSpace(r))
				o.paint(g, r);
			}else if(o instanceof LevelProp){
				Rect r = schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ());
				if(inScreenSpace(r))
				o.paint(g, r);
			}
		}
		}

		g.setColor(new Color(30,30,30,160));
		g.fillRect((int)LEVEL_SCREEN_SPACE.getX(), (int)LEVEL_SCREEN_SPACE.getY(), (int)LEVEL_SCREEN_SPACE.getWidth(), (int)LEVEL_SCREEN_SPACE.getHeight());

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(1));
		
		for(int x1 = -buff+(int)TOPLEFT_BOUND.x;x1<BOTTOMRIGHT_BOUND.x+buff;x1++){
			for(int y1 = -buff+(int)TOPLEFT_BOUND.y;y1<BOTTOMRIGHT_BOUND.y+buff;y1++){
				Point p = new Point(x1*Globals.GRIDSIZE-location.x, y1*Globals.GRIDSIZE-location.y);
			
				Rect r1 = new Rect((int)p.x, (int)p.y, (int)(Globals.GRIDSIZE), (int)(Globals.GRIDSIZE));
				if(inScreenSpace(r1)){
				g.drawLine((int)p.x, (int)p.y, (int)(p.x), (int)(p.y+Globals.GRIDSIZE));//down
				
				g.drawLine((int)p.x, (int)p.y, (int)(p.x+Globals.GRIDSIZE), (int)(p.y));//right
				}
			}
		}

		g.setColor(Color.ORANGE);
		g.setStroke(new BasicStroke(2));
		for(Collider o : colliders) {
			Rect r = schemToLocal(o, location);
			if(inScreenSpace(r)&&o.visible())
			g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
		}
		
		

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));

		

		DrawUtil.DrawRect(g, PLAYER_SCREEN_LOC, Color.RED);

		for(GameObject o : objects) {
			if(o.getZ()>1){
				if(o instanceof ColorRect){
					Rect r = schemToLocal(o, location);
					if(inScreenSpace(r))
					o.paint(g, r);
				}else if(o instanceof LevelProp){
					Rect r = schemToLocalZ(o, PLAYER_SCREEN_LOC, location, o.getZ());
					if(inScreenSpace(r))
					o.paint(g, r);
				}
			}
		}
		g.setColor(Color.BLUE);
		g.drawRect((int)LEVEL_SCREEN_SPACE.getX(), (int)LEVEL_SCREEN_SPACE.getY(), (int)LEVEL_SCREEN_SPACE.getWidth(), (int)LEVEL_SCREEN_SPACE.getHeight());
	}
	static Rect schemToLocalZ(Rect r, Rect PLAYER_SCREEN_LOC, Point location, double z)
	{
	Point p = new Point(
		r.getX() * Globals.GRIDSIZE * z - location.x * z - (PLAYER_SCREEN_LOC.getX() * z - PLAYER_SCREEN_LOC.getX()) + (r.getWidth() * Globals.GRIDSIZE / 2 * z - r.getWidth() * Globals.GRIDSIZE / 2) - (PLAYER_SCREEN_LOC.getWidth() / 2 * z - PLAYER_SCREEN_LOC.getWidth() / 2),
		r.getY() * Globals.GRIDSIZE * z - location.y * z - PLAYER_SCREEN_LOC.getY() * z + PLAYER_SCREEN_LOC.getY() + r.getHeight() * Globals.GRIDSIZE * z - r.getHeight() * Globals.GRIDSIZE - PLAYER_SCREEN_LOC.getHeight() * z + PLAYER_SCREEN_LOC.getHeight());
	return new Rect(p.x, p.y, r.getWidth()*Globals.GRIDSIZE, r.getHeight()*Globals.GRIDSIZE);
	}

	static Rect schemToLocal(Rect r, Point location)
	{
	///multiply by Globals.GRIDSIZE, subtract camera location
	return new Rect(r.getX() * Globals.GRIDSIZE - location.x, r.getY() * Globals.GRIDSIZE - location.y, r.getWidth() * Globals.GRIDSIZE, r.getHeight() * Globals.GRIDSIZE);
	}
	
	
	public void onResize(double width, double height) {
		PLAYER_SCREEN_LOC = new Rect((width-Globals.PLAYER_SIZE)/2,(height-Globals.PLAYER_SIZE)/2,Globals.PLAYER_SIZE,Globals.PLAYER_SIZE);
	}
	
	public void onTick(long tick) {
		
		double component_x = 0;
		double component_y = 0;
		
		int intent_x = 0;
		int intent_y = 0;
		if(keyPress(KeyEvent.VK_W))
			intent_y++;
		if(keyPress(KeyEvent.VK_S))
			intent_y--;
		if(keyPress(KeyEvent.VK_D))
			intent_x++;
		if(keyPress(KeyEvent.VK_A))
			intent_x--;

		if(!(intent_x==0 && intent_y==0)){
		double angle = Math.atan2(intent_y, intent_x);
		component_x = Globals.PLAYER_SPEED/Globals.REFRESH_RATE*Math.cos(angle);
		component_y = Globals.PLAYER_SPEED/Globals.REFRESH_RATE*Math.sin(angle);
		}

		location.x+=component_x;
		location.y-=component_y;
	}

	void levelUpdated(){
		TOPLEFT_BOUND = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
		BOTTOMRIGHT_BOUND = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE);
	for (Rect o: objects)
	{
		if (o.getX() < TOPLEFT_BOUND.x)
			TOPLEFT_BOUND.x = o.getX();
		if (o.getX()+o.getWidth() > BOTTOMRIGHT_BOUND.x)
			BOTTOMRIGHT_BOUND.x = o.getX()+o.getWidth();
		if (o.getY() < TOPLEFT_BOUND.y)
			TOPLEFT_BOUND.y = o.getY();
		if (o.getY()+o.getHeight() > BOTTOMRIGHT_BOUND.y)
			BOTTOMRIGHT_BOUND.y = o.getY()+o.getHeight();
	}
	for (Rect o: colliders)
	{
		if (o.getX() < TOPLEFT_BOUND.x)
			TOPLEFT_BOUND.x = o.getX();
		if (o.getX()+o.getWidth() > BOTTOMRIGHT_BOUND.x)
			BOTTOMRIGHT_BOUND.x = o.getX()+o.getWidth();
		if (o.getY() < TOPLEFT_BOUND.y)
			TOPLEFT_BOUND.y = o.getY();
		if (o.getY()+o.getHeight() > BOTTOMRIGHT_BOUND.y)
			BOTTOMRIGHT_BOUND.y = o.getY()+o.getHeight();
	}
	}
	
	boolean keyPress(int i) {
		return entry.peripherals.KeyPressed(i);
	}

	boolean inScreenSpace(Rect r){
		return CollisionUtil.staticCollision(new Rect(0,0,this.getWidth(), this.getHeight()), r);
	}
	
}
