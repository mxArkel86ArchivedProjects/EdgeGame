package util;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Globals;
import main.entry;

public class ImageImport {
	public static BufferedImage getImage(String path) {
		try {
			BufferedImage image = ImageIO.read(new File(path));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void ImportImageAssets() {
		for (final File fileEntry : new File("assets").listFiles()) {
	        if (fileEntry.isFile()) {
				try{
	           String name = fileEntry.getName().substring(0, fileEntry.getName().indexOf("."));//no file extension
	           BufferedImage img = resize(getImage(fileEntry.getPath()), Globals.ASSET_SIZE, Globals.ASSET_SIZE);
	           
	           entry.app.assets.put(name, img);
				}catch(Exception e){
					
				}
	        }
	    }
		
	}
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
	
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
	
		return dimg;
	}  
}
