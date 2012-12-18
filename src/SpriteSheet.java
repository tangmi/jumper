import java.awt.*;


public class SpriteSheet {
	Image spriteSheet;
	int frames, frameRate, sheetx, sheety, w, h;
	
	public SpriteSheet(Image source, int frames, int frameRate, int x, int y, int w, int h) {
		spriteSheet = source;
		this.frames = frames;
		this.frameRate = frameRate;
		this.sheetx = x;
		this.sheety = y;
		this.w = w;
		this.h = h;
	}
	
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
	//draw and animate the sprite based on the sheet
	public void draw(Graphics g, int x, int y, int facing, int clock) {
		int frame = clock * frameRate / JumperMain.TICKS_PER_SECOND % frames;
		int l = 0; int r = 0;
		if(facing == 1) {
			r = 1;
		} else if(facing == -1) {
			l = 1;
		}
		g.drawImage(spriteSheet, x + w * l, y, x + w * r, y + h, 
				sheetx + w * frame, sheety, sheetx + w + w * frame, sheety + h, null);
	}
}
