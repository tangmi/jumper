import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

//contains all the tile information
class Tile extends GameObject {
	public static final String SPRITE_SHEET = "tiles.gif";
	BufferedImage img;
	SpriteSheet tiles;
	boolean allLoaded;
	
	public Tile() {
		//for loading the images only
	}
	
	public Tile(int x, int y, BufferedImage img) {
		super(x, y);
		allLoaded = false;
		this.img = img;
		super.setCollisionBox(0, 0, dim.width, dim.height);
		solid = true;
	}
	
	//load the image
	public BufferedImage loadImage() {
		try {
		    return ImageIO.read(getClass().getResource(SPRITE_SHEET));
		} catch (IOException e) {}
		return null;
	}

	public void draw(Graphics g, int x, int y) {
		tiles.draw(g, x, y, 1, 0);
	}
}

//crate. yeah.
class Crate extends Tile {
	public Crate(int x, int y, BufferedImage img) {
		super(x, y, img);
		tiles = new SpriteSheet(img, 1, 0, 0, 4 * Level.GRID_SIZE, dim.width, dim.height);
	}
}

//nonsolid, for decoration
class Plant extends Tile {
	public Plant(int x, int y, BufferedImage img) {
		super(x, y, img);
		solid = false;
		tiles = new SpriteSheet(img, 1, 0, 0, 5 * Level.GRID_SIZE, dim.width, dim.height);
	}
}

//solid, grass, uses lazytile
class Grass extends Tile {
	private boolean isSpecialTileRight, isSpecialTileLeft;
	private SpriteSheet specialTiles;
	public Grass(int x, int y, BufferedImage img) {
		super(x, y, img);
	}
	
	public void onLoad() {
		int offset = findTile();
		isSpecialTileRight = collision(pos.x + Level.GRID_SIZE, pos.y, "Grass", true) &&
				collision(pos.x, pos.y - Level.GRID_SIZE, "Grass", true) && 
				!collision(pos.x + Level.GRID_SIZE, pos.y - Level.GRID_SIZE, "Grass", true);
		isSpecialTileLeft = collision(pos.x - Level.GRID_SIZE, pos.y, "Grass", true) &&
				collision(pos.x, pos.y - Level.GRID_SIZE, "Grass", true) && 
				!collision(pos.x - Level.GRID_SIZE, pos.y - Level.GRID_SIZE, "Grass", true);;
		if(isSpecialTileRight || isSpecialTileLeft) {
			specialTiles = new SpriteSheet(img, 1, 0, Level.GRID_SIZE, 1 * Level.GRID_SIZE, dim.width, dim.height);
		}

		tiles = new SpriteSheet(img, 1, 0, offset * Level.GRID_SIZE, 0, dim.width, dim.height);
	}
	
	//draw correct tile and any special ones
	public void draw(Graphics g, int x, int y) {
		super.draw(g, x, y);
		if(isSpecialTileRight) {
			specialTiles.draw(g, x, y, 1, 0);
		}
		if(isSpecialTileLeft) {
			specialTiles.draw(g, x, y, -1, 0);
		}
	}
	
	//return the int corresponding to the premade sprite sheet
	private int findTile() {
		String collisionData = "";
		collisionData += collision(pos.x, pos.y + Level.GRID_SIZE, "Grass", true) ? "1" : "0";
		collisionData += collision(pos.x + Level.GRID_SIZE, pos.y, "Grass", true) ? "1" : "0";
		collisionData += collision(pos.x, pos.y - Level.GRID_SIZE, "Grass", true) ? "1" : "0";
		collisionData += collision(pos.x - Level.GRID_SIZE, pos.y, "Grass", true) ? "1" : "0";
		return Integer.parseInt(collisionData,2);
	}
}