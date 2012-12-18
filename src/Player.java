import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.sound.sampled.*;



public class Player extends GameObject implements KeyListener {
	private static final int STANDING = 1;
	private static final int WALKING = 2;
	private static final int JUMPING = 3;
	public Velocity vel;
	public boolean onGround;
	boolean keyLeft, keyRight, keyJump;
	double friction, runAcceleration, airAcceleration, maxFallSpeed;
	double speed, maxSpeed, runSpeed;
	boolean isRunning;
	double jumpSpeed;
	double runJumpBoost; //extra kick to jump speed at max speed
	private int facing;
	SpriteSheet spriteStand, spriteWalk, spriteRun, spriteJump;
	int state;
	boolean canJump; //0=ready;1=jump

	public Player(int x, int y) {
		super(x, y);
		this.vel = new Velocity();
		this.speed = 3;
		this.maxSpeed = 4;
		this.runSpeed = 3.9;
		this.runAcceleration = 1.5;
		this.airAcceleration = 1;
		this.jumpSpeed = 10;
		this.runJumpBoost = 15; //boost = (vel.x - runSpeed) * this number = ~1.3
		this.friction = 0.5;
		this.maxFallSpeed = (int) (1.5 * jumpSpeed);
		this.dim = new Dimension(16, 32);
		box = new Rectangle(3, 13, dim.width - 6, dim.height - 13);
		this.canJump = true;

		this.facing = 1;
		
		this.state = STANDING;
		
		//load images
		BufferedImage spriteSheet = null;
		try {
		    spriteSheet = ImageIO.read(getClass().getResource("hero.gif"));
		} catch (IOException e) {}	
		spriteStand = new SpriteSheet(spriteSheet, 16, 5, 0, 0, dim.width, dim.height);
		spriteWalk = new SpriteSheet(spriteSheet, 3, 10, 0, 32, dim.width, dim.height);
		spriteRun = new SpriteSheet(spriteSheet, 3, 15, 0, 32, dim.width, dim.height);
		spriteJump = new SpriteSheet(spriteSheet, 1, 1, 0, 64, dim.width, dim.height);
	}

	public void update() {
		
		if(!onGround && collision(pos.x, pos.y - 1, "", true)) {
			if(Math.abs(vel.y) > 2 * speed) {
				playSound(hitHardSound);
			} else {
				playSound(hitSoftSound);
			}
			vel.y = 0;
		}
		if(!collision(pos.x, pos.y + 1, "", true)) {
			vel.y += GRAVITY;
			onGround = false;
		} else {
			if(!onGround) {
				if(collision(pos.x, pos.y + 1, "Crate", true)) {
					playSound(landCrateSound);
				} else {
					playSound(landSound);
				}
			}
			vel.y = 0;
			onGround = true;
		}
		if(pos.y > JumperMain.height) {
			pos.y = -dim.height;
		}
		vel.y = Math.min(vel.y, maxFallSpeed);
		
		if(collision(pos.x + (int) Math.signum(vel.x), pos.y, "", true)) {
			if(vel.x != 0) {
				if(isRunning) {
					playSound(hitSoftSound);
				}
			}
		}
		if(vel.x != 0 && onGround) {
			//playSound(walkSound);
		}
		
		if(keyLeft || keyRight) {
			if(!(keyLeft && keyRight)) {
				double tempAcceleration = onGround ? runAcceleration : airAcceleration;
				if(keyLeft && !keyRight) {
					if(vel.x > 0) {
						vel.x = 0;
					}
					if(Math.abs(vel.x) < maxSpeed) {
						vel.x-= (tempAcceleration / speed) + ((tempAcceleration / maxSpeed) / speed) * vel.x;
					}
					facing = -1;
				}
				if(keyRight && !keyLeft) {
					if(vel.x < 0) {
						vel.x = 0;
					}
					if(Math.abs(vel.x) < maxSpeed) {
						vel.x += (tempAcceleration / speed) - ((tempAcceleration / maxSpeed) / speed) * vel.x;
					}
					facing = 1;
				}
			} else {
				slowDueToFriction();
			}
		} else { //neither left or right is pressed
			slowDueToFriction();
		}
		
		if(Math.abs(vel.x) > runSpeed) {
			if(onGround) {
				if(isRunning == false) {
					//create dust effect here
					playSound(walkSound);
				}
				isRunning = true;
			}
		} else {
			isRunning = false;
		}
		
		if(keyJump && onGround && canJump) {
			playSound(jumpSound);
			vel.y = -jumpSpeed;
			if(isRunning) {
				double boost = (Math.abs(vel.x) - runSpeed) * runJumpBoost;
				vel.y -= boost;
			}
			canJump = false;
		}
		
		
		if(collision(pos.x + (int) Math.signum(vel.x), pos.y, "", true)) {
			vel.x = 0;
		}
		
		
		//set state
		if(!onGround) {
			state = JUMPING;
		} else {
			state = (vel.x == 0) ? STANDING : WALKING;
		}
		move(vel.x, vel.y);
	}

	//move the player with michaelengine's precision
	public void move(double xvel, double yvel) {
		int dx = isRunning ? (int) Math.round(xvel) : (int) xvel;
		int dy = (int) Math.round(yvel);
		if(!collision(pos.x + dx, pos.y, "", true)) {
			pos.translate(dx, 0);
		} else {
			int stepx = (int) Math.signum(dx);
			for(int i = 0; i <= Math.abs(dx); i++) {
				if(!collision(pos.x + stepx, pos.y, "", true)) {
					pos.translate(stepx, 0);
				} else {
					break;
				}
			}
		}
		if(!collision(pos.x, pos.y + dy, "", true)) {
			pos.translate(0, dy);
		} else {
			int stepy = (int) Math.signum(dy);
			for(int i = 0; i < Math.abs(dy); i++) {
				if(!collision(pos.x, pos.y + stepy, "", true)) {
					pos.translate(0, stepy);
				} else {
					break;
				}
			}
		}
	}
	
	public void slowDueToFriction() {
		if(Math.abs(vel.x) > friction) {
			if(onGround) {
				vel.x -= friction * facing;
			} else {
				vel.x -= (friction * facing) / 2;
			}
		}
		if(Math.abs(vel.x) <= friction) {
			vel.x = 0;
		}
	}
	
	String jumpSound = "jump.wav";
	String hitSoftSound = "hitSoft.wav";
	String hitHardSound = "hitHard.wav";
	String landSound = "land.wav";
	String landCrateSound = "landCrate.wav";
	String walkSound = "walk.wav";
	public void playSound(String file) {
		try {
			Clip sound = AudioSystem.getClip();
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(file));
			sound.open(inputStream);
			sound.start(); 
		} catch (Exception e) {}
	}

	//grab clock state
	public void setClock(int clock) {
		this.clock = clock;
	}
	
	//draw the sprite
	public void draw(Graphics g, int x, int y) {
		if(state == JUMPING) {
			spriteJump.draw(g, x, y, facing, clock);
		} else if(state == WALKING) {
			if(isRunning) {
				spriteRun.draw(g, x, y, facing, clock);
			} else {
				spriteWalk.draw(g, x, y, facing, clock);
			}
		} else if(state == STANDING) {
			spriteStand.draw(g, x, y, facing, clock);
		}		
//		if(isRunning) {
//			g.setColor(Color.RED);
//			g.drawRect(x,y,dim.width,dim.height);
//		}
	}
	

	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
		if(id == KeyEvent.VK_LEFT) {
			keyLeft = true;
		}
		if(id == KeyEvent.VK_RIGHT) {
			keyRight = true;
		}
		if(id == KeyEvent.VK_UP && canJump) {
			keyJump = true;
		}
		
		e.consume();
	}
	public void keyReleased( KeyEvent e ) {
		int id = e.getKeyCode();
		if(id == KeyEvent.VK_LEFT) {
			keyLeft = false;
		}
		if(id == KeyEvent.VK_RIGHT) {
			keyRight = false;
		}
		if(id == KeyEvent.VK_UP) {
			keyJump = false;
			canJump = true;
			if(vel.y < 0) {
				vel.y /= 2;
			}
		}
	}
	public void keyTyped( KeyEvent e ) { }
	
	class Velocity {
		public double x, y;
	}
}
