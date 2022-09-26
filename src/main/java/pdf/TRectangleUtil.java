package pdf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class TRectangleUtil {

	static boolean mDebug = false;

	//debug
	static int[] mColorCacheR = new int[1000000];
	static int[] mColorCacheG = new int[1000000];
	static int[] mColorCacheB = new int[1000000];
	static int mColorCount = 0;
	//debug

	static public void printRects(List<TRectangle> list){
			if(list != null) {
				System.out.print("F ");
				for(int j = 0; j < list.size(); j++){
					System.out.print(list.get(j).printString());
				}
				System.out.println("");
			}
	}


	static public void printRects(TreeMap<Integer,List<TRectangle>> map){
		for(int i = 0; i < map.size(); i++){
			List<TRectangle> list = map.get(i);
			if(list != null) {
				System.out.print("C["+i+"]");
				for(int j = 0; j < list.size(); j++){
					System.out.print(list.get(j).printString());
				}
				System.out.println("");
			}
		}
	}


	static public void PrintEdges(TreeMap<Integer,List<TEdge>> map){
		for(int i = 0; i < map.size(); i++){
			List<TEdge> list = map.get(i);
			if(list != null) {
				System.out.print("E["+i+"]");
				for(int j = 0; j < list.size(); j++){
					System.out.print(list.get(j).printString());
				}
				System.out.println("");
			}
		}
	}



	static public void RectToBitmap(int w, int h, TreeMap<Integer,List<TRectangle>> map, String outputfilename) throws IOException{
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics gs = img.getGraphics();

		for(int i = 0; i < map.size(); i++){
			List<TRectangle> list = map.get(i);
			if(list != null) {
		        for(TRectangle tr : list){
					Color c = tr.getColor().getAWTColor();
					if(mDebug){
						int index = tr.getID();
						c = getColor(index);
					}
					gs.setColor(c);
					gs.fillRect(tr.getSX(), tr.getSY(), tr.getW(), tr.getH());
		        }
			}
		}

		gs.dispose();
		ImageIO.write(img, "png", new File(outputfilename+".png"));
	}

	static public void RectToBitmap2(int w, int h, TreeMap<Integer,List<TRectangle>> map, String outputfilename) throws IOException{
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics gs = img.getGraphics();

        int count = 0;
		for(int i = 0; i < map.size(); i++){
			List<TRectangle> list = map.get(i);
			if(list != null) {
		        for(TRectangle tr : list){
					Color c = getColor(++count);
					gs.setColor(c);
					gs.fillRect(tr.getSX(), tr.getSY(), tr.getW(), tr.getH());
		        }
			}
		}

		gs.dispose();
		ImageIO.write(img, "png", new File(outputfilename+".png"));
	}



	static public void EdgeToBitmap(int w, int h, TreeMap<Integer,List<TEdge>> map, String outputfilename) throws IOException{
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics gs = img.getGraphics();

		for(int i = 0; i < map.size(); i++){
			List<TEdge> list = map.get(i);
			if(list != null) {
				for(int j = 0; j < list.size(); j++){
					TEdge e = list.get(j);
					int sx = e.getSX();
					int ex = e.getEX();
					Color c = e.getColor().getAWTColor();
					if(mDebug){
						int index = e.getID();
						c = getColor(index);
					}
					gs.setColor(c);
					gs.fillRect(sx, i, ex - sx + 1, 1);
				}
			}
		}

		gs.dispose();
		ImageIO.write(img, "png", new File(outputfilename+".png"));

	}


	static public void RectToBitmap(int w, int h, List<TRectangle>list, String outputfilename) throws IOException{
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Graphics gs = img.getGraphics();

		//draw back
		TColor tc = new TColor();
		gs.setColor(tc.getAWTColor());
		gs.fillRect(0, 0, w, h);

		if(list != null) {
			for(TRectangle tr : list){
				Color c = tr.getColor().getAWTColor();
				if(mDebug){
					int index = tr.getID();
					c = getColor(index);
				}
				gs.setColor(c);
				gs.fillRect(tr.getSX(), tr.getSY(), tr.getW(), tr.getH());
			}
		}

		gs.dispose();
		ImageIO.write(img, "png", new File(outputfilename+".png"));
	}

	static public void RectToBitmap2(int w, int h, List<TRectangle>list, String outputfilename) throws IOException{
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Graphics gs = img.getGraphics();

		//draw back
		TColor tc = new TColor();
		gs.setColor(tc.getAWTColor());
		gs.fillRect(0, 0, w, h);

		int count = 0;
		if(list != null) {
			for(TRectangle tr : list){
				Color c = getColor(++count);
				gs.setColor(c);
				gs.fillRect(tr.getSX(), tr.getSY(), tr.getW(), tr.getH());
			}
		}

		gs.dispose();
		ImageIO.write(img, "png", new File(outputfilename+".png"));
	}


	static private Color getColor(int index) {
		int r, g, b;
		if(index == 0){
		    r = 180;
		    g = 180;
		    b = 180;
		    mColorCacheR[mColorCount] = r;
		    mColorCacheG[mColorCount] = g;
		    mColorCacheB[mColorCount] = b;
		    mColorCount ++;
		}
		else if(mColorCount > index){
			r = mColorCacheR[index];
			g = mColorCacheG[index];
			b = mColorCacheB[index];
		}
		else{
		    Random rand = new Random();
		    r = rand.nextInt(128);
		    g = rand.nextInt(128);
		    b = rand.nextInt(128);
		    mColorCacheR[mColorCount] = r;
		    mColorCacheG[mColorCount] = g;
		    mColorCacheB[mColorCount] = b;
		    mColorCount ++;
		}
		Color c = new Color(r, g, b);
		return c;
	}

}
