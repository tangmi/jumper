import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;


public class EffectGenerator {
	public static final String SPRITE_SHEET = "effect.gif";
	private BufferedImage sheet;
	private Set<Effect> effectSet;
	
	public EffectGenerator() {
		try {
		    sheet = ImageIO.read(getClass().getResource(SPRITE_SHEET));
		} catch (IOException e) {}
		effectSet = new HashSet<Effect>();
		
	}
	public void update() {
		Iterator<Effect> i = effectSet.iterator();
		while(i.hasNext()) {
			Effect effect = i.next();
			if(effect.done) {
				i.remove();
			} else {
				effect.update();
			}
		}
	}
	public void createEffect(int x, int y, int type) {
		effectSet.add(new Effect(sheet, x, y, type));
	}
	public void draw(Graphics g) {
		for(Effect effect : effectSet) {
			effect.draw(g);
		}
	}
}
