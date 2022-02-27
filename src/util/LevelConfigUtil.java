package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.Color;

import gameObjects.Collider;
import gameObjects.ColorRect;
import gameObjects.GameObject;
import gameObjects.LevelProp;
import gameObjects.ResetBox;
import main.entry;

public class LevelConfigUtil {
    public static void saveLevel(){
        try {
            
            BufferedWriter w = new BufferedWriter(new FileWriter("output.txt"));
            w.write("-=-=-=-= output =-=-=-=-=-\n\n");
            w.write("[collision]\n");
            for(Collider c : entry.app.newColliders){
                w.write(String.format("%d,%d,%d,%d,T\n",(int)c.x, (int)c.y, (int)(c.x+c.width),(int)(c.y+c.height)));
            }
            w.write("\n[levelprop]\n");
            for(GameObject c : entry.app.newObjects){
                if(c instanceof LevelProp){
                    LevelProp p = (LevelProp)c;
                    w.write(String.format("%s,%d,%d,%d,%d,%.3f\n",p.getAsset(), (int)c.x, (int)c.y, (int)(c.x+c.width),(int)(c.y+c.height), p.getZ()));
                }
            }
            w.write("\n[colorrect]\n");
            for(GameObject c : entry.app.newObjects){
                if(c instanceof ColorRect){
                    ColorRect p = (ColorRect)c;
                    w.write(String.format("%s,%d,%d,%d,%d,%.3f\n",p.getColor(), (int)c.x, (int)c.y, (int)(c.x+c.width),(int)(c.y+c.height), p.getZ()));
                }
            }

            entry.app.newColliders.clear();
            entry.app.newObjects.clear();
            w.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLevel(){
        entry.app.colliders.clear();
        entry.app.colors.clear();
        entry.app.objects.clear();
        entry.app.checkpoints.clear();

        BufferedReader reader;
        String category = "";
		try {
			reader = new BufferedReader(new FileReader("level.txt"));
			String line = reader.readLine();
            
            while (line != null) {
                String ln = line;
                
                line = reader.readLine();
                if(ln.startsWith("[")){
                    category = ln.substring(1, ln.length()-1);
                    continue;
                }
                if(category.length()==0)
                    continue;
                if(ln.length()==0)
                    continue;
                if(ln.startsWith("#"))
                    continue;
                
                String[] args = ln.split(",");
                if(category.contentEquals("collision")){
                   double x1 = Double.parseDouble(args[0]);
                   double y1 = Double.parseDouble(args[1]);
                   double x2 = Double.parseDouble(args[2]);
                   double y2 = Double.parseDouble(args[3]);
                   boolean v = args[4].equalsIgnoreCase("T");

                   double x = Math.min(x1, x2);
                   double y = Math.min(y1, y2);
                   double w = Math.abs(x1-x2);
                   double h = Math.abs(y1-y2);
                   Collider c = new Collider(x, y, w, h, v);
                   entry.app.colliders.add(c);
                }else if(category.contentEquals("colorrect")){
                    String color = args[0];
                    double x1 = Double.parseDouble(args[1]);
                    double y1 = Double.parseDouble(args[2]);
                    double x2 = Double.parseDouble(args[3]);
                    double y2 = Double.parseDouble(args[4]);
                    double z = Double.parseDouble(args[5]);
 
                    double x = Math.min(x1, x2);
                    double y = Math.min(y1, y2);
                    double w = Math.abs(x1-x2);
                    double h = Math.abs(y1-y2);
                    ColorRect c = new ColorRect(x, y, w, h, z, color);
                    entry.app.objects.add(c);
                 }else if(category.contentEquals("color")){
                    String color = args[0];
                    String hex = "#"+ args[1];

                    Color c = Color.decode(hex);
                    entry.app.colors.put(color, c);
                 } else if(category.contentEquals("levelprop")){
                    String name = args[0];
                    double x1 = Double.parseDouble(args[1]);
                    double y1 = Double.parseDouble(args[2]);
                    double x2 = Double.parseDouble(args[3]);
                    double y2 = Double.parseDouble(args[4]);
                    double z = Double.parseDouble(args[5]);
 
                    double x = Math.min(x1, x2);
                    double y = Math.min(y1, y2);
                    double w = Math.abs(x1-x2);
                    double h = Math.abs(y1-y2);
                    LevelProp c = new LevelProp(x, y, w, h, z, name);
                    entry.app.objects.add(c);
                 } else if(category.contentEquals("checkpoint")){
                    String name = args[0];
                    double x1 = Double.parseDouble(args[1]);
                    double y1 = Double.parseDouble(args[2]);
                    Point p =  new Point(x1, y1);
                    entry.app.checkpoints.put(name, p);
                 }else if(category.contentEquals("resetbox")){
                    String name = args[0];
                    double x1 = Double.parseDouble(args[1]);
                    double y1 = Double.parseDouble(args[2]);
                    double x2 = Double.parseDouble(args[3]);
                    double y2 = Double.parseDouble(args[4]);
                    
                    double x = Math.min(x1, x2);
                    double y = Math.min(y1, y2);
                    double w = Math.abs(x1-x2);
                    double h = Math.abs(y1-y2);
                    ResetBox b = new ResetBox(x, y, w, h, name);
                    entry.app.resetboxes.add(b);
                 }


				
			}
            reader.close();

        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
}
