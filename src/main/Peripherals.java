package main;

import java.awt.event.*;
import java.util.*;

import util.Point;


public class Peripherals implements ComponentListener, KeyListener, MouseMotionListener {
	HashMap<Integer, Boolean> keyRegister = new HashMap<Integer, Boolean>();
	Point MOUSE_POS = new Point(0,0);
	
	@Override
	public void componentResized(ComponentEvent e) {
		entry.app.onResize(entry.app.getWidth(), entry.app.getHeight());
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		keyRegister.put(key, true);
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		keyRegister.put(key, false);
	}
	
	public boolean KeyPressed(int keycode) {
		return keyRegister.getOrDefault(keycode, false);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		MOUSE_POS = new Point(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		MOUSE_POS = new Point(e.getX(), e.getY());
	}

	public Point mousePos(){
		return MOUSE_POS;
	}

}
