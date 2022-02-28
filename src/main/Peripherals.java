package main;

import java.awt.event.*;
import java.util.*;

import javax.swing.event.MouseInputListener;

import util.Point;


public class Peripherals implements ComponentListener, KeyListener, MouseInputListener {
	HashMap<Integer, Boolean> keyRegister = new HashMap<Integer, Boolean>();
	Point MOUSE_POS = new Point(0,0);
	String typed_str = "";
	boolean type_enable = false;
	
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
		if(type_enable)
		typed_str+=e.getKeyChar();
	}

	public void typingEnable(boolean b){
		type_enable = b;
	}

	public String keysTyped(){
		String str = typed_str;
		typed_str = "";
		return str;
	}
	public boolean keysTypedB(){
		return typed_str.length()>0;
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

	@Override
	public void mouseClicked(MouseEvent e) {
		Point pos = new Point(e.getX(), e.getY());
		entry.app.mouseClick(pos);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
