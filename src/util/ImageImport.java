package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.entry;

public class ImageImport {
	public static BufferedImage getImage(String path) {
		try {
			BufferedImage image = ImageIO.read(new File(path));
			return image;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void ImportImageAssets() {
		for (final File fileEntry : new File("assets").listFiles()) {
	        if (fileEntry.isFile()) {
	           String name = fileEntry.getName().substring(0, fileEntry.getName().indexOf("."));//no file extension
	           BufferedImage img = getImage(fileEntry.getPath());
	           
	           entry.app.assets.put(name, img);
	        }
	    }
		
	}
}
