import java.awt.*;
import java.util.*;


public class Viewport {
	protected Point pos;
	protected Dimension dim;
	
	public Viewport(int x, int y, int w, int h) {
		this.pos = new Point(x, y);
		this.dim = new Dimension(w, h);
	}
	
	public void move(int x, int y) {
		//don't let it show outside level
		this.pos.x = Math.min(JumperMain.width - dim.width, Math.max(0, x));
		this.pos.y = Math.min(JumperMain.height - dim.height, Math.max(0, y));
	}
	
	public void moveNoBounds(int x, int y) {
		this.pos.x = x;
		this.pos.y = y;
	}
	
	public Point getPosition() {
		return pos;
	}
	
	public Dimension getSize() {
		return dim;
	}
	
	protected ArrayList<GameObject> things;
	public void setGameObjects(ArrayList<GameObject> things) {
		this.things = things;
	}
	
	private static final int DRAW_PADDING = 32;
	public void draw(Graphics g) {
		if(things != null) {
			for(GameObject thing : things) {
				if(thing.pos.x > this.pos.x - Level.GRID_SIZE - DRAW_PADDING && thing.pos.x < this.pos.x + this.dim.width + DRAW_PADDING &&
						thing.pos.y > this.pos.y - Level.GRID_SIZE - DRAW_PADDING && thing.pos.y < this.pos.y + this.dim.width + DRAW_PADDING) {
					thing.draw(g, thing.pos.x - this.pos.x, thing.pos.y - this.pos.y);
					thing.inViewport = true;
				} else {
					thing.inViewport = false;
				}
			}
		}
	}
	
	//for the editor
	public void draw(Graphics g, SortedSet<EditorObject> editorThings) {
		if(editorThings != null) {
			for(EditorObject thing : editorThings) {
				thing.draw(g, thing.pos.x - this.pos.x, thing.pos.y - this.pos.y);

				//we don't care about hiding shit
//				if(thing.pos.x > this.pos.x - Level.GRID_SIZE - DRAW_PADDING && thing.pos.x < this.pos.x + this.dim.width + DRAW_PADDING &&
//						thing.pos.y > this.pos.y - Level.GRID_SIZE - DRAW_PADDING && thing.pos.y < this.pos.y + this.dim.width + DRAW_PADDING) {
//					thing.draw(g, thing.pos.x - this.pos.x, thing.pos.y - this.pos.y);
//				}
			}
		}
	}
}
