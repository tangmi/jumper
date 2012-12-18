import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class Level {
	private ArrayList<GameObject> things;
	private int levelWidth, levelHeight;
	public static final int GRID_SIZE = 16;
	private String file;
	public static final String FILE_EXTENSION = "tang"; //.tang level files

	public Level(String file) {
		this.file = file;
		things = new ArrayList<GameObject>();
	}

	//build the map/places objects based on the stored url file
	public void build() throws FileNotFoundException {
		try{
			String line;
//			int j = 0;
//			int maxI = 0;
			
			int maxx = 0;
			int maxy = 0;
			InputStream in = getClass().getResourceAsStream(file + "." + FILE_EXTENSION);
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			if(JumperMain.DEBUG) System.out.print("Placing objects...");
			Tile imageLoader = new Tile();
			BufferedImage img = imageLoader.loadImage();
			while((line = bf.readLine()) != null){
				String[] temp = line.split(":");
				String[] objects = temp[1].split(";");
				for(String object : objects) {
					String[] data = object.split(",");
					int type = Integer.parseInt(data[0]);
					Point pos = new Point(Integer.parseInt(data[2]), Integer.parseInt(data[3]));
					if(pos.x > maxx) {
						maxx = pos.x;
					}
					if(pos.y > maxy) {
						maxy = pos.y;
					}
					addObject(type, pos.x, pos.y, img);
				}
				
				
				
//				if(JumperMain.DEBUG) System.out.print(".");
//				
//				int length = line.length();
//				if(length > maxI) {
//					maxI = length;
//				}
//				if(!line.startsWith("//") && line.length() > 0) { //ignore comments and blank lines
//					if(line.startsWith("{")) { //part of non-grid placement
//						String[] data = line.substring(1, line.length() - 1).split(",");
//						String object = data[0];
//						int x = Integer.parseInt(data[1]);
//						int y = Integer.parseInt(data[2]);
//						addObject(object, x, y);
//					} else {
//						for(int i = 0; i < length; i++) {
//							addObject(line.substring(i, i+1), i * GRID_SIZE, j * GRID_SIZE);			
//						}
//						j++;
//					}
//				}
			}
//			levelHeight = j * GRID_SIZE;
//			levelWidth = maxI * GRID_SIZE;
			
			levelWidth = maxx + GRID_SIZE;
			levelHeight = maxy + GRID_SIZE;
			if(JumperMain.DEBUG) System.out.println("done");

		} catch(IOException e) {}
	}
	
//	//declare some constants for the file format, air is anything not mentioned ("-")
//	private static final String CRATE = "c";
//	private static final String GRASS = "g";
//	private static final String PLAYER = "s";
//	private static final String PLANT = "p";
//	//add an object based on a passed check string
//	public void addObject(String check, int x, int y) {
//		if(check.equals(CRATE))
//			things.add(new Crate(x, y));
//		if(check.equals(GRASS))
//			things.add(new Grass(x, y));
//		if(check.equals(PLAYER))
//			things.add(new Player(x, y));
//		if(check.equals(PLANT))
//			things.add(new Plant(x, y));
//	}
	
	public void addObject(int type, int x, int y, BufferedImage spriteSheet) {
		if(type == ObjectType.CRATE)
			things.add(new Crate(x, y, spriteSheet));
		if(type == ObjectType.GRASS)
			things.add(new Grass(x, y, spriteSheet));
		if(type == ObjectType.PLANT)
			things.add(new Plant(x, y, spriteSheet));
		if(type == ObjectType.PLAYER)
			things.add(new Player(x, y));
	}
	
	public int getLevelWidth() {
		return levelWidth;
	}
	public int getLevelHeight() {
		return levelHeight;
	}

	public ArrayList<GameObject> getGameObjects() {
		return things;
	}

}
