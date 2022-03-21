package util;

import java.util.HashMap;

import main.Globals;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class AssetSet {
    private HashMap<Size, BufferedImage> assets = new HashMap<>();
    
    BufferedImage base = null;
    public AssetSet(BufferedImage img) {
        base = img;
    }

    public int addAsset(double schemwidth, double schemheight) {
        BufferedImage newasset = ImageImport.resize(base, (int)(schemwidth * Globals.PIXELS_PER_GRID),
                (int)(schemheight * Globals.PIXELS_PER_GRID));
        assets.put(new Size(schemwidth, schemheight), newasset);
        return assets.size() - 1;
    }

    public BufferedImage getAsset(int index) {
        return ((BufferedImage[]) assets.values().toArray())[index];
    }
    
    public BufferedImage getAsset(double width, double height) {
        for (Size s : assets.keySet()) {
            if (s.width == width && s.height == height) {
                BufferedImage img = assets.get(s);
                return img;
            }
        }
        return null;
    }

    public BufferedImage getBaseAsset() {
        return base;
    }
}
