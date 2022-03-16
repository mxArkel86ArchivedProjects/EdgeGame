package util;

import main.entry;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

public class ScreenAnimation {
    static final Font DEATHSCREEN_TEXT = new Font("Arial", Font.BOLD, 48); 

    public static boolean DeathScreen_Enabled(long deathscreen_tick){
        if(entry.tick-deathscreen_tick>3000)
            return false;
        return true;
    }

    public static void DeathScreen_Graphics(Graphics2D g, long deathscreen_tick, int width, int height){
        int a = 255;
        if (entry.tick - deathscreen_tick > 2000)
            a = 255 - (int) Math.min(255, (entry.tick - deathscreen_tick - 2000) * 255 / 1000);
        // System.out.println("out=" + a);
        g.setFont(DEATHSCREEN_TEXT);
        g.setColor(new Color(0, 0, 0, a));
        g.fillRect(0, 0, width, height);
        g.setStroke(new BasicStroke(2));

        g.setColor(new Color(255, 255, 255, a));
        String str = "Dead";
        Rectangle2D r2d = g.getFontMetrics().getStringBounds(str, 0, str.length(), g);
        g.drawString(str, (int) ((width - r2d.getWidth()) / 2),
                (int) ((height + g.getFontMetrics().getAscent()) / 2));
    }
}
