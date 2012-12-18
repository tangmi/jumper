import java.awt.*;
import java.awt.image.*;


public class Effect {
	public static final int SKID = 1;

	SpriteSheet effect;
	int x, y;
	int clock;
	int length;
	int type;
	boolean done;
	
	public Effect(BufferedImage sheet, int x, int y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;
		if(type == SKID) {
			length = 6;
			effect = new SpriteSheet(sheet, length, 30, 0, 0, 8, 8);
		}
	}
	
	public void update() {
		if(clock >= length) {
			done = true;
		}
		clock++;
	}
	
	public void draw(Graphics g, int facing) {
		effect.draw(g, x, y, facing, clock);
		g.setColor(Color.RED);
		g.drawRect(x, y, 8, 8);
		System.out.println("tried to draw effect " + type + " at " + x + ", " + y);
	}
	
	public void draw(Graphics g) {
		draw(g, 1);
	}
}
