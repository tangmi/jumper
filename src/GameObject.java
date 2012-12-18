import java.awt.*;
import java.awt.event.*;
import java.util.*;

//basic class containing position, dimension, and collision information
public class GameObject implements KeyListener {
	public Point pos;
	protected Rectangle box;
	protected Dimension dim;
	public static final int GRAVITY = 1;
	protected boolean solid = false;
	public boolean inViewport;
	
	public GameObject() {
		//for making an empty game object
	}
	
	public GameObject(int x, int y) {
		this.pos = new Point(x, y);
		this.dim = new Dimension(16,16);
		this.box = new Rectangle(0, 0, dim.width, dim.height);
	}
	
	public void setCollisionBox(int x, int y, int w, int h) {
		box = new Rectangle(x, y, w, h);
	}
	
	//get a list of all objects currently used for collision detection
	protected ArrayList<GameObject> things;
	public void setGameObjects(ArrayList<GameObject> things) {
		this.things = things;
	}
	
	//checks for a collision at x, y, for type object (or all, if ""), and if the object type is solid
	public boolean collision(int x, int y, String object, boolean solid) {
		boolean collision = false;
		for(GameObject thing : things) {
			if(object.equals("") || thing.getClass().getName().equals(object)) {
				if(thing.solid == solid) {
					Rectangle temp = new Rectangle(thing.box.x + thing.pos.x, thing.box.y + thing.pos.y,
							thing.box.width, thing.box.height);
					collision = temp.intersects(x + box.x, y + box.y, box.width, box.height);
				}
			}
			if(collision) break;
		}
		return collision;
	}
	
	int clock; //for animations
	public void setClock(int clock) {}
	public void update() {}
	public void onLoad() {}
	public void draw(Graphics g, int x, int y) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
