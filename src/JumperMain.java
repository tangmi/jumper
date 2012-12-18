import java.applet.*;
import java.awt.*;
import java.io.*;
import java.util.*;

@SuppressWarnings("serial")
public class JumperMain extends Applet implements Runnable {
	public static final int TICKS_PER_SECOND = 30;
	public static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
	public static final int MAX_FRAMESKIP = 10;
	public static final Dimension VIEW_SIZE = new Dimension(455, 256);
	
	public static final boolean IS_GAME_TROTTLED = true; //intentionally slow down the game to save processes.
	public static final boolean DEBUG = true; //show debug messages
	
	private boolean skyIsBlue = true; //runs the main game loops

	private Thread t;
	private Level level1;
	private ArrayList<GameObject> things;
	protected static int width, height;
	private Image backbuffer;
	private Graphics backg;
	private Color skycolor;
	private int clock; //for animations
	
	protected Viewport viewport1;
//	private int maxDelay;
//	private double interpolation;
	
	public void init() {		
		skycolor = new Color(152, 224, 230);
		setBackground(skycolor);

		//for timing how long it takes to build the level;
		long start, time;
		if(JumperMain.DEBUG) start = System.currentTimeMillis();
		
		
		//Viewport creation code (must go before any object placement
		if(JumperMain.DEBUG) System.out.print("Initializing viewport...");
		viewport1 = new Viewport(100,0,VIEW_SIZE.width,VIEW_SIZE.height);
		viewport1.setGameObjects(things);
		if(JumperMain.DEBUG) System.out.println("done");
		
		
		//Level building code from file
		if(JumperMain.DEBUG) System.out.print("Initializing level...");
		String level = "testlevel"; //no extension
		level1 = new Level(level);
		if(JumperMain.DEBUG) System.out.println("done");
		try {
			level1.build();
		} catch(FileNotFoundException e) {e.printStackTrace();}
		width = level1.getLevelWidth();
		height = level1.getLevelHeight();
		
		
		//Resizing the applet to viewport size
		if(JumperMain.DEBUG) System.out.print("Resizing applet...");
		resize(viewport1.dim.width, viewport1.dim.height);
		if(JumperMain.DEBUG) System.out.println("done");
		
		
		//Add listeners and initialize all the objects
		if(JumperMain.DEBUG) System.out.print("Initializing objects");
		things = level1.getGameObjects();
		for(GameObject thing : things) {
			if(JumperMain.DEBUG) System.out.print(".");
			addKeyListener(thing);
			thing.setGameObjects(things);
			thing.onLoad();
		}
		if(JumperMain.DEBUG) System.out.println("done");
		
		
		//Initialize the image buffer
		if(JumperMain.DEBUG) System.out.print("Creating image buffer...");
		backbuffer = createImage(viewport1.dim.width, viewport1.dim.height);
		backg = backbuffer.getGraphics();
		backg.setColor(Color.BLACK);
		if(JumperMain.DEBUG) System.out.println("done");

		//Create the thread and start running the applet
		if(JumperMain.DEBUG) System.out.print("Starting thread...");
		t = new Thread(this);
		t.start();
		if(JumperMain.DEBUG) System.out.println("done");
		
		//Output relevant level build information and add object count and setup time to buildtime.cvs
		if(JumperMain.DEBUG) {
			time = System.currentTimeMillis() - start;
			System.out.println();
			System.out.println("Level information");
			System.out.println("   Filename     : " + level);
			System.out.println("   Object count : " + things.size());
			System.out.println("   Setup time   : " + time + "ms");
			System.out.println("   Dimensions   : (" + width + ", " + height + ")");
			try{
				String logfile = "buildtime.csv";
				FileWriter fstream = new FileWriter(logfile,true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(things.size() + "," + time + "\n");
				out.close();
			}catch (Exception e){}
			System.out.println("   Max processing time information:");
		}
	}
	

	public void run() {
		long tm = System.currentTimeMillis();
		int loops;
		while(skyIsBlue) {
			loops = 0;
			while(System.currentTimeMillis() > tm && loops < MAX_FRAMESKIP) {
				things = level1.getGameObjects();
				for(GameObject thing : things) {
					thing.setGameObjects(things);
					thing.setClock(clock);
					thing.update();
					if(thing.getClass().getName().equals("Player")) {
						//smooth camera pan
						int x = viewport1.pos.x + (thing.pos.x - viewport1.pos.x - viewport1.dim.width / 2 + thing.dim.width / 2) / 4;
						int y = viewport1.pos.y + (thing.pos.y - viewport1.pos.y - viewport1.dim.height / 2 + thing.dim.height / 2) / 4;
						viewport1.move(x, y);
					}
				}
				viewport1.setGameObjects(things);
				clock++;
				if(JumperMain.DEBUG) {if(loops > 0) {System.out.println("      Skipped frame " + clock + "! loops = " + loops);}}
				tm += SKIP_TICKS;
				loops++;

				if(JumperMain.DEBUG) showStatus("Frame: " + clock); else
				showStatus("jumper by tang");
			}
			
			
//			interpolation = (1.0 * System.currentTimeMillis() + SKIP_TICKS - tm) / (1.0 * SKIP_TICKS);
			repaint();
			
			if(IS_GAME_TROTTLED) {
				try {
					Thread.sleep(MAX_FRAMESKIP / 2);
				} catch (InterruptedException e) {}
			}
			//sleep
			// try {
			// 	if(JumperMain.DEBUG) {
			// 		showStatus("Frame: " + clock + ", offset (ms): " + (nextGameTick - System.currentTimeMillis()));
			// 		if(maxDelay > (nextGameTick - System.currentTimeMillis())) {
			// 			maxDelay = (int) (nextGameTick - System.currentTimeMillis());
			// 			if(maxDelay + frameDelay < 0) {
			// 				System.out.println("      Frame: " + clock + ", time: " + (-maxDelay) + "/" + frameDelay + "; Process time longer than sleep time!");
			// 				maxDelay = 0;
			// 			}
			// 		}
			// 	}
			// 	nextGameTick += SKIP_TICKS;
			// 	Thread.sleep(Math.max(0, nextGameTick - System.currentTimeMillis()));
			// } catch(InterruptedException e) {
			// 	e.printStackTrace();
			// }
			// clock++;
		}
	}
	
	//overrides the default update() method, for the double backbuffer
	public void update(Graphics g) {
		backg.setColor(skycolor);
		backg.fillRect(0, 0, width, height);
		viewport1.draw(backg);
		g.drawImage(backbuffer, 0, 0, this);
	}

	public void paint(Graphics g) {
		update(g);
	}
}

