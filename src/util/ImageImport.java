package util;

import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

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

	public static VolatileImage getVolatileImage(String path, GraphicsConfiguration gc) {
		try {
			BufferedImage image = ImageIO.read(new File(path));

			return toVolatile(image, gc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static VolatileImage toVolatile(BufferedImage src, GraphicsConfiguration gc) {
		try {
			ImageCapabilities icap = new ImageCapabilities(false);
			VolatileImage img = gc.createCompatibleVolatileImage(src.getWidth(), src.getHeight(), icap);
			img.getGraphics().drawImage(src, 0, 0, null);
			img.flush();
			return img;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
	public static VolatileImage resizeV(VolatileImage img, GraphicsConfiguration gc, int newW, int newH) { 
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING);
		VolatileImage dimg = gc.createCompatibleVolatileImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
	
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
	
		return dimg;
	}

	public static BufferedImage flippedImage(BufferedImage img) {
		// Flip the image horizontally
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-img.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		img = op.filter(img, null);
		return img;
	}  
}
