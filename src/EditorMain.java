import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;

@SuppressWarnings("serial")
public class EditorMain extends Applet implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	public static final boolean DEBUG = true;
	public static final int FPS = 30;
	public static final int GRID_SIZE = 16;
	
	private SortedSet<EditorObject> things; //temporarily contains all object data for the session

	private int editorSize; //editor size in GRID_SIZEs
	private int width, height; //size of available editor area
	private Dimension levelDim; //size of level area
	private Point levelPos; //position of level area
	private boolean showPlayerView;
	
	private Viewport view;
	private Point viewPoint; //point to follow the view
	private Dimension viewDim; //size of viewport
	
	private Point mouse; //mouse cords
	private Point cell; //cell cords
	private int gridFactor;
	private int gridSize;
	
	private int z; //z-index
	private boolean gridPlacementMode;
	private int selectedType;

	private int frameDelay;
	private Image backbuffer;
	private Graphics backg;
	private Thread t;
	
	private EditorObject selectedObject;
	private boolean isDragging;
	private boolean leftClick;
	private boolean rightClick;
	
	private boolean loading; // to tell if editor is loading
	private String saveFile = "untitled"; //file to load/save
	private boolean saved; //tells user if file is saved of not
	
	BufferedImage tilesImg;

//	Frame frame = new Frame("hello");

	public void init() {
//		frame.setVisible(true);
//		frame.setSize(100,480);
//		frame.setLocation(660, 50);
		
		if(DEBUG) System.out.print("Initializing...");
		
		frameDelay = 1000 / FPS;

		editorSize = 100;
		width = editorSize * GRID_SIZE;
		height = editorSize * GRID_SIZE;
		showPlayerView = true; //show what the player would see
		
		viewDim = new Dimension(640, 480); 
		view = new Viewport(0, 0, viewDim.width, viewDim.height);
		resize(view.getSize().width, view.getSize().height);
		viewPoint = new Point(0, height / 2 - viewDim.height / 2); //initial view point
		
		
		backbuffer = createImage(width, height);
		backg = backbuffer.getGraphics();
		backg.setColor(Color.BLACK);
		mouse = new Point();
		cell = new Point();
		levelDim = new Dimension(-width, -height); //make it super negative
		levelPos = new Point();
		things = new TreeSet<EditorObject>();

		selectedType = ObjectType.AIR;
		selectedObject = new EditorObject(0, 0, 0, selectedType, null);

		gridPlacementMode = true;
		
		gridFactor = 1;
		gridSize = GRID_SIZE / gridFactor;

		z = 0;

		t = new Thread(this);
		t.start();
		
		try {
		    tilesImg = ImageIO.read(getClass().getResource(EditorObject.TILES_SHEET));
		} catch (IOException e) {}	

		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		if(DEBUG) System.out.println("done");
		if(DEBUG) System.out.println();
		if(DEBUG) System.out.println("JUMPER EDITOR by tang");
		if(DEBUG) System.out.println("  > type ? for help");
	}

	public void run() {
		long tm = System.currentTimeMillis();
		while(true) {
			view.moveNoBounds(viewPoint.x, viewPoint.y);

			cell.x = mouse.x / gridSize;
			cell.y = mouse.y / gridSize;

			String msg = "";
			if(!loading) {
				if(gridPlacementMode) {
					msg += "Cell location: (" + cell.x + ", " + cell.y + ")";
				} else {
					msg += "Object location: (" + mouse.x + ", " + mouse.y + ")";
				}
				msg += ", " + things.size() + " objects";
				msg += ", viewport at " + "(" + viewPoint.x + ", " + viewPoint.y + ")";
			} else {
				msg = things.size() + " objects loaded";
			}
			showStatus(msg);


			repaint();
			try {
				tm += frameDelay;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void addObject(Point pos, int zindex, int type) {
		if(type != ObjectType.AIR) {
			int preSize = things.size();
			things.add(new EditorObject(pos.x, pos.y, zindex, type, tilesImg));
			if(DEBUG && preSize < things.size()) System.out.println("+ Added object type " + type + " at (" + pos.x + ", " + pos.y + ")");
			updateSize();
			saved = false;
		}
	}
	public void addObjectGrid(Point cell, int zindex, int type) {
		addObject(new Point(cell.x * gridSize, cell.y * gridSize), zindex, type);
	}
	public void removeObject(int x, int y) {
		Iterator<EditorObject> i = things.iterator();
		while(i.hasNext()) {
			EditorObject thing = i.next();
			if(x > thing.pos.x && x < thing.pos.x + gridSize && y > thing.pos.y && y < thing.pos.y + gridSize) {
				if(DEBUG) System.out.println("- Removed object type " + thing.type + " at (" + thing.pos.x + ", " + thing.pos.y + ")");
				i.remove();
				updateSize();
				saved = false;
			}
		}
	}

	private boolean updateSize() {
		int maxx = 0;
		int maxy = 0;
		int minx = editorSize * GRID_SIZE;
		int miny = editorSize * GRID_SIZE;
		boolean changed = false;
		for(EditorObject thing : things) {
			if(thing.pos.x > maxx) {
				maxx = thing.pos.x;
			}
			if(thing.pos.y > maxy) {
				maxy = thing.pos.y;
			}
			if(thing.pos.x < minx) {
				minx = thing.pos.x;
			}
			if(thing.pos.y < miny) {
				miny = thing.pos.y;
			}
		}
		if(levelDim.width != maxx - minx || levelDim.height != maxy - miny) {
			changed = true;
			if(DEBUG) System.out.println("  > Updated level size to (" + (maxx - minx) + ", " + (maxy - miny) + ")");
		}
		levelDim.width = maxx - minx;
		levelDim.height = maxy - miny;
		levelPos.move(minx, miny);
		return changed;
	}

	
	/*
	 * Level file is in this format:
	 * offsetx,offsety:type,z,x,y;type,z,x,y;...
	 * offsetx, offsety, and z are for the editor only
	 * 
	 * TODO: add information for viewport start location to the metadata
	 */
	public void changeSaveFile() {
		saveFile = JOptionPane.showInputDialog(null, "What is the file you want to use? (no extension)", "Change working file", JOptionPane.QUESTION_MESSAGE);
		saveFile += "." + Level.FILE_EXTENSION;
	}
	public void load(String inputFile) throws FileNotFoundException {
		things.clear();
		Scanner in = new Scanner(new File(inputFile));
		if(DEBUG) System.out.println("Loading from " + inputFile);
		loading = true;
		while(in.hasNextLine()) {
			String line = in.nextLine();
			String[] meta = line.split(":");
			String[] offset = meta[0].split(",");
			Point offsetPos = new Point(Integer.parseInt(offset[0]), Integer.parseInt(offset[1]));
			if(DEBUG) System.out.println("Level offset set to (" + Integer.parseInt(offset[0]) + ", " + Integer.parseInt(offset[1]) + ")");
			String objectData = meta[1];
			String[] objects = objectData.split(";");
			for(String object : objects) {
				String[] data = object.split(",");
				int type = Integer.parseInt(data[0]);
				int zindex = Integer.parseInt(data[1]);
				Point pos = new Point(Integer.parseInt(data[2]) + offsetPos.x, Integer.parseInt(data[3]) + offsetPos.y);
				addObject(pos, zindex, type);
			}
		}
		if(DEBUG) System.out.println("Done, " + things.size() + " objects loaded");
		loading = false;
	}
	public void export(String outputFile) throws FileNotFoundException {
		PrintStream out = new PrintStream(new File(outputFile));
		if(DEBUG) System.out.println("Saving to " + outputFile);
		out.print(levelPos.x + "," + levelPos.y + ":");
		for(EditorObject thing : things) {
			out.print(thing.type + "," + thing.z + ","+ (thing.pos.x - levelPos.x) + "," + (thing.pos.y - levelPos.y) + ";");
			if(DEBUG) System.out.println("= Writing object type " + thing.type + " at (" + (thing.pos.x - levelPos.x) + ", " + (thing.pos.y - levelPos.y) + ")");
		}
		out.close();
		saved = true;
		if(DEBUG) System.out.println("Done, " + things.size() + " objects written");
	}

	public void keyPressed(KeyEvent e) {
		int i = e.getKeyCode();
		int step = GRID_SIZE / 2;
		if(i == KeyEvent.VK_UP) {
			viewPoint.translate(0, -step);
		} else if(i == KeyEvent.VK_DOWN) {
			viewPoint.translate(0, step);
		} else if(i == KeyEvent.VK_LEFT) {
			viewPoint.translate(-step, 0);
		} else if(i == KeyEvent.VK_RIGHT) {
			viewPoint.translate(step, 0);
		}
		if(i == KeyEvent.VK_CONTROL) {
			isDragging = true;
		}
		char c = e.getKeyChar();
		if(c >= '1' && c <= '8') {
			int temp = Integer.parseInt("" + c);
			gridFactor = temp;
			if(DEBUG) System.out.println("o Set grid factor to " + gridFactor);
			gridSize = GRID_SIZE / gridFactor;
		}
		if(c == '9') {
			showPlayerView = !showPlayerView;
		}
		if(c == 'h' || c == 'H' || c == '?') {
			showHelp();
		}
		if(c == 'f') {
			changeSaveFile();
		}
		if(c == '=') {
			z++;
		} else if(c == '-') {
			z--;
		}
		if(c == '\\') {
			gridPlacementMode = !gridPlacementMode;
			if(DEBUG) {String msg = gridPlacementMode ? "Grid" : "Free"; System.out.println(msg + " placement mode selected");}
		}
		if(c == 'g') {
			selectedType = ObjectType.GRASS;
			if(DEBUG) System.out.println("Selected type GRASS");
		} else if(c == 'c') {
			selectedType = ObjectType.CRATE;
			if(DEBUG) System.out.println("Selected type CRATE");
		} else if(c == 'p') {
			selectedType = ObjectType.PLANT;
			if(DEBUG) System.out.println("Selected type PLANT");
		} else if(c == 's') {
			selectedType = ObjectType.PLAYER;
			if(DEBUG) System.out.println("Selected type PLAYER");
		}
		
		if(c == 'e') {
			try {
				export(saveFile);
			} catch (FileNotFoundException e1) {}
		} else if(c == 'l') {
			try {
				load(saveFile);
			} catch (FileNotFoundException e1) {}
		} 
		selectedObject.setType(selectedType);
		e.consume();
	}
	public void keyReleased(KeyEvent e) {
		int i = e.getKeyCode();
		if(i == KeyEvent.VK_CONTROL) {
			isDragging = false;
		}
		e.consume();
	}
	public void keyTyped(KeyEvent e) {}


	

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(!gridPlacementMode) {
				addObject(new Point(mouse.x - gridSize / 2, mouse.y - gridSize / 2), z, selectedType);
			} else {
				addObjectGrid(cell, z, selectedType);
			}
			leftClick = true;
		}
		if(e.getButton() == MouseEvent.BUTTON3) {
			removeObject(mouse.x, mouse.y);
			rightClick = true;
		}
//		e.consume(); //doesn't allow keyboard events if consumed, for some reason
	}
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3) {
			rightClick = false;
		}
		if(e.getButton() == MouseEvent.BUTTON1) {
			leftClick = false;
		}
		e.consume();
	}
	public void mouseMoved(MouseEvent e) {
		Point temp = e.getPoint();
		mouse.move(temp.x + view.pos.x, temp.y + view.pos.y);
		e.consume();
	}
	public void mouseDragged(MouseEvent e) {
		Point temp = e.getPoint();
		mouse.move(temp.x + view.pos.x, temp.y + view.pos.y);
		if(leftClick && isDragging) {
			if(!gridPlacementMode) {
				addObject(new Point(mouse.x - gridSize / 2, mouse.y - gridSize / 2), z, selectedType);
			} else {
				addObjectGrid(cell, z, selectedType);
			}
		}
		if(rightClick && isDragging) {
			removeObject(mouse.x, mouse.y);
		}
		e.consume();
	}

	public void update(Graphics g) {
		backg.setColor(Color.WHITE);
		backg.fillRect(0, 0, width, height);

		view.draw(backg, things);

		backg.setColor(new Color(200,200,200));
		backg.drawString("EDITOR BOUNDS", -view.pos.x, -view.pos.y);
		for(int i = 0; i <= width / gridSize; i++) {
			backg.drawLine(-view.pos.x + i * gridSize, -view.pos.y, -view.pos.x + i * gridSize, -view.pos.y + height);
		}
		for(int i = 0; i <= height / gridSize; i++) {
			backg.drawLine(-view.pos.x, -view.pos.y + i * gridSize, -view.pos.x + width, -view.pos.y + i * gridSize);
		}

		int xx, yy;
		if(gridPlacementMode) {
			xx = cell.x * gridSize;
			yy = cell.y * gridSize;
		} else {
			xx = mouse.x - gridSize / 2;
			yy = mouse.y - gridSize / 2;
		}
		xx -= view.pos.x;
		yy -= view.pos.y;

		backg.setColor(Color.RED);
		backg.drawString("LEVEL BOUNDS", -view.pos.x + levelPos.x, -view.pos.y + levelPos.y);
		backg.drawRect(-view.pos.x + levelPos.x, -view.pos.y + levelPos.y, levelDim.width + GRID_SIZE, levelDim.height + GRID_SIZE);

		selectedObject.move(xx, yy);
		backg.setColor(Color.RED);
		backg.drawString("TYPE" + selectedType, xx, yy);
		backg.drawRect(xx, yy, selectedObject.dim.width, selectedObject.dim.height);

//		selectedObject.draw(backg);

		if(showPlayerView) {
			backg.setColor(Color.BLUE);
			backg.drawString("PLAYER VIEW", view.dim.width / 2 - JumperMain.VIEW_SIZE.width / 2, view.dim.height / 2 - JumperMain.VIEW_SIZE.height / 2);
			backg.drawRect(view.dim.width / 2 - JumperMain.VIEW_SIZE.width / 2, view.dim.height / 2 - JumperMain.VIEW_SIZE.height / 2, 
					JumperMain.VIEW_SIZE.width, JumperMain.VIEW_SIZE.height);
		}
		
		backg.setColor(Color.WHITE);
		backg.fillRect(0, 0, view.dim.width, 24);
		backg.setColor(Color.GRAY);
		backg.drawLine(0, 24, view.dim.width, 24);
		backg.drawLine(view.dim.width, 0, view.dim.width, 24);
		backg.setColor(Color.BLACK);
		selectedObject.draw(backg, 8, 4);
		backg.drawString("type: " + selectedType + "   z-index: " + z + "   file: " + saveFile + (saved ? "" : "*"), 32, 16);
		
		backg.drawString("type ? for help", view.dim.width - 100, 16);


		g.drawImage(backbuffer, 0, 0, null);

	}

	public void paint(Graphics g) {
		update(g);
	}
	

	public void showHelp() {
		System.out.println("HOTKEY LIST");
		showHelpLine("\\", "change placement mode");
		showHelpLine("ctrl", "hold for multi-place");
		showHelpLine("1-8", "change grid factoring");
		showHelpLine("9", "toggle player view box");
		showHelpLine("f", "change working file");
		showHelpLine("e", "export to " + saveFile);
		showHelpLine("l", "load from " + saveFile);
		showHelpLine("-", "lower z-index (further backward)");
		showHelpLine("+", "raise z-index (further forward)");
		showHelpLine("arrows", "move around the editor");
		showHelpLine("c", "select CRATE brush");
		showHelpLine("g", "select GRASS brush");
		showHelpLine("p", "select PLANT brush");
		showHelpLine("s", "select PLAYER START brush");
	}
	private void showHelpLine(String key, String info) {
		int spacing = 7;
		System.out.format("%-"+spacing+"s", key);
		System.out.println(info);
	}
}
