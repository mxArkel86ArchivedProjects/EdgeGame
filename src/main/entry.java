package main;

import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class entry {
public static JFrame frame;
public static Application app;
public static Peripherals peripherals;
public static Timer t;
public static long tick = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		frame = new JFrame();
		t = new Timer();
		app = new Application();
		peripherals = new Peripherals();

		int h = Globals.INITIAL_HEIGHT;
		int w = Globals.INITIAL_WIDTH;
		
		frame.setSize(new Dimension(w, h));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		app.Init(w, h);
		
		//System.setProperty("sun.java2d.opengl", "true");
		
		// t.scheduleAtFixedRate(new TimerTask() {

		// 	@Override
		// 	public void run() {
		// 		tick++;
		// 		app.onTick(tick);
				
		// 	}
			
		// }, 0, 10);
		
		frame.addComponentListener(peripherals);
		frame.addKeyListener(peripherals);
		app.addMouseMotionListener(peripherals);
		
		Thread thread=new Thread(() ->
        {
        	long diff = (long)(1000000000l/Globals.REFRESH_RATE);
        	long reg = 0;
            while(true)
            {
                long time=System.nanoTime();
                if(time>=reg+diff) {
                try { 
                	reg = time;
                	SwingUtilities.invokeAndWait(() ->{
					app.onTick(tick++);
                    app.repaint(0,0, app.getWidth(), app.getHeight());
                	});
                
                }
                catch(Exception e) { 
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
