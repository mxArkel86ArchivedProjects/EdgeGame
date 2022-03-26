package main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

//import net.java.games.input.Controller;

public class entry {
public static JFrame frame;
public static Application app;
public static Peripherals peripherals;
public static Timer t;
public static long tick = 0;

	public static void main(String[] args) {
		frame = new JFrame();
		t = new Timer();
		app = new Application();
		peripherals = new Peripherals();

		int h = Globals.INITIAL_HEIGHT;
		int w = Globals.INITIAL_WIDTH;
		
		frame.setSize(new Dimension(w, h));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		app.Init(w, h);
		
		System.setProperty("sun.java2d.opengl", "true");
		
				// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			cursorImg, new Point(0, 0), "blank cursor");

		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);

		System.setProperty("sun.java2d.opengl", "true");
		
		frame.addComponentListener(peripherals);
		frame.addKeyListener(peripherals);
		app.addMouseMotionListener(peripherals);
		app.addMouseListener(peripherals);
		
		//Controller c = peripherals.getControllers()[0];

		Thread thread=new Thread(() ->
        {
			long diff = (long) (1000000000l / Globals.REFRESH_RATE);
			long diff2 = (long)(1000000000l/120);
			long reg = 0;
			long reg2 = 0;
            while(true)
            {
                long time=System.nanoTime();
				tick = time/1000000;
				if (time >= reg + diff) {
					try {
						reg = time;
						SwingUtilities.invokeAndWait(() -> {
							//peripherals.ControllerTick(c);
							//app.onTick();
							app.repaint(0, 0, app.getWidth(), app.getHeight());
						});

					} catch (Exception e) {
						((Throwable) e).getStackTrace();
					}
				}
				 if (time >= reg2 + diff2) {
				 	try {
						reg2 = time;
						SwingUtilities.invokeAndWait(() -> {
							app.onTick();
						});

					} catch (Exception e) {
						((Throwable) e).getStackTrace();
					}
				}
                try {
                //Thread.sleep(16L);
                }catch(Exception e) {
                	
                }
            }
        });
        thread.start();
		
		
        frame.add(app);
        frame.setVisible(true);
	}
	

}
