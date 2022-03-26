package main;

import java.awt.event.*;
import java.util.*;

import javax.swing.event.MouseInputListener;

//import net.java.games.input.Component;
//import net.java.games.input.Controller;
//import net.java.games.input.ControllerEnvironment;
//import net.java.games.input.Event;
//import net.java.games.input.EventQueue;
import util.Point;


public class Peripherals implements ComponentListener, KeyListener, MouseInputListener {
	HashMap<Integer, Boolean> keyRegister = new HashMap<Integer, Boolean>();
	HashMap<Integer, Boolean> keyToggleRegister = new HashMap<Integer, Boolean>();
	//List<HashMap<Integer, Boolean>> controllersRegister = new ArrayList<>();
	Point MOUSE_POS = new Point(0,0);
	String typed_str = "";
	boolean type_enable = false;
	public boolean mouse_state = false;
	
	
//	public Controller[] getControllers() {
//		return ControllerEnvironment.getDefaultEnvironment().getControllers();
//	}
//	
//	
//	public void ControllerTick(Controller controller) {
//		if(controller!=null) {
//		Event event = new Event();
//		    /* Remember to poll each one */
//		    controller.poll();
//
//		    /* Get the controllers event queue */
//		    EventQueue queue = controller.getEventQueue();
//
//		    /* For each object in the queue */
//		 while (queue.getNextEvent(event)) {
//		        /* Get event component */
//				Component comp = event.getComponent();
//				String input_name = comp.getName();
//				System.out.println(input_name);
//		 }
//		
//		}
//	}
	
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

	public boolean KeyToggled(int keycode){
		boolean b = keyRegister.getOrDefault(keycode, false);
		boolean b_toggle = keyToggleRegister.getOrDefault(keycode, false);
		if(b){
			if(b_toggle){
				keyToggleRegister.put(keycode, false);
				return true;
			}else
			return false;
		}else{
			keyToggleRegister.put(keycode, true);
			return false;
		}
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
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point pos = new Point(e.getX(), e.getY());
		entry.app.mouseClick(pos);
		mouse_state = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouse_state = false;
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
