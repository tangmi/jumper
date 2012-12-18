import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;


public class EditorObject implements Comparable<EditorObject> {
	public static final String TILES_SHEET = "tiles.gif";
	public static final String HERO_SHEET = "hero.gif";
	Point pos;
	Dimension dim; // for drawing
	protected int type;
	Image img;
	SpriteSheet tiles;
	int z;
	BufferedImage tileImg, heroImg;
	
	public EditorObject(int x, int y, int z, int type, BufferedImage tilesImg) {
		pos = new Point(x, y);
		this.z = z;
		this.type = type;
		tileImg = null;
		dim = new Dimension(EditorMain.GRID_SIZE, EditorMain.GRID_SIZE);
	    this.tileImg = tilesImg;
	    if(type == 4) {
			try {
			    heroImg = ImageIO.read(getClass().getResource(EditorObject.HERO_SHEET));
	
			} catch (IOException e) {}	
	    }
		setType(type);
	}
	
	public void move(int x, int y) {
		pos.move(x, y);
	}
	
	public void setType(int type) {
		this.type = type;
		dim.height = EditorMain.GRID_SIZE;
		if(type == ObjectType.AIR) {
			tiles = new SpriteSheet(tileImg, 1, 0, 3 * EditorMain.GRID_SIZE, EditorMain.GRID_SIZE, dim.width, dim.height);
		} else if(type == ObjectType.GRASS) {
			tiles = new SpriteSheet(tileImg, 1, 0, 0, 0, dim.width, dim.height);
		} else if(type == ObjectType.CRATE) {
			tiles = new SpriteSheet(tileImg, 1, 0, 0, 4 * EditorMain.GRID_SIZE, dim.width, dim.height);
		} else if(type == ObjectType.PLANT) {
			tiles = new SpriteSheet(tileImg, 1, 0, 0, 5 * EditorMain.GRID_SIZE, dim.width, dim.height);
		} else if(type == ObjectType.PLAYER) {
			dim.height = 2 * EditorMain.GRID_SIZE;
			tiles = new SpriteSheet(heroImg, 1, 0, 0, 0, dim.width, dim.height);
		}
	}
	
	public String toString() {
		return "" + type + "(" + pos.x + ", " + pos.y + ")";
	}
	
	public void draw(Graphics g, int x, int y) {
		tiles.draw(g, x, y, 1, 0);
	}
	
	public void draw(Graphics g) {
		draw(g, pos.x, pos.y);
	}

	public int compareTo(EditorObject other) {
		if(this.z - other.z == 0) {
			if(this.pos.x - other.pos.x == 0) {
				return this.pos.y - other.pos.y;	
			} else {
				return this.pos.x - other.pos.x;
			}
		} else {
			return this.z - other.z;
		}
	}
	
}
