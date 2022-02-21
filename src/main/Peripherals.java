package main;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class Peripherals implements ComponentListener, KeyListener {
	HashMap<Integer, Boolean> keyRegister = new HashMap<Integer, Boolean>();
	
	
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

}
