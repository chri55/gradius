import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Gradius implements ActionListener, KeyListener {

	public static Gradius gradius;
	public Renderer renderer = new Renderer();
	public final int WIDTH = 900, HEIGHT = 600;
	public Random rand = new Random(), gameOverMessage = new Random();
	public Rectangle ship;
	public ArrayList<Rectangle> pipes, enemies, bullets = new ArrayList<Rectangle>(),
			stars = new ArrayList<Rectangle>(), powers = new ArrayList<Rectangle>(),
			powerShots = new ArrayList<Rectangle>(), superBullets = new ArrayList<Rectangle>();
	public boolean started = false, gameOver = false, keepShootin = false, paused = false, paintPaused = false,
			superShot = false;
	public int ticks, yMotion = 2, score, gOM, pauseState = 0, difficulty = 0, powerUp, shotMovement = 2;
	public Rectangle[] enemiesArray[];
	public int lives = 3;
	public boolean up, down, left, right;

	public static void main(String[] args) {
		gradius = new Gradius();

	}

	public Gradius() {
		JFrame frame = new JFrame();
		Timer timer = new Timer(20, this);

		frame.add(renderer);
		frame.setSize(WIDTH, HEIGHT + 200);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("parodius");
		frame.setVisible(true);
		frame.addKeyListener(this);

		ship = new Rectangle(WIDTH / 2, HEIGHT / 2, 44, 44);
		enemies = new ArrayList<Rectangle>();
		for (int i = 0; i <= 100; i++) {
			stars.add(new Rectangle(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), 2, 2));
		}

		addPipe(1);
		addPipe(1);
		addPipe(1);
		addPipe(1);

		timer.start();
	}

	private void addPipe(int start) {
		int width = 100;
		int distance = 500; // Distance between enemies
		

		if (start == 1) {
			Enemy e = new Enemy(WIDTH + width + (enemies.size() + 1) * distance, rand.nextInt(HEIGHT - 100), 50, 100);
			enemies.add(e.spawn());
			
		} else if (start == 2) {
			System.out.println("EMPTY ARRAY??");
			enemies.add(new Rectangle(WIDTH + width + distance, rand.nextInt(HEIGHT - 100), 50, 100));

		} else {
			enemies.add(new Rectangle(WIDTH + width + enemies.size() * distance, rand.nextInt(HEIGHT - 100), 50, 100));
		}

		for (int i = 0; i <= 99; i++) {
			stars.add(new Rectangle(rand.nextInt(250) + WIDTH, rand.nextInt(HEIGHT), 2, 2));
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (!paused) {
			paintPaused = false;
			update();
		} else {
			paintPaused = true;
			renderer.repaint();
		}
	}

	private void update() {
		int enemySpeed = 3 * difficulty; // speed of the enemies
	
		ticks++;

		if (up) {
			if (ship.y > 0) {
				ship.y -= 5 * difficulty;
			} else
				ship.y = 0;

		}
		if (down) {
			if (ship.y + ship.height < HEIGHT) {
				ship.y += 5 * difficulty;
			} else
				ship.y = HEIGHT - ship.height;

		}
		if (left) {
			if (ship.x > 0) {
				ship.x -= 5 * difficulty;
			} else
				ship.x = 0;
		}
		if (right) {
			if (ship.x + ship.width < WIDTH) {
				ship.x += 5 * difficulty;
			} else
				ship.x = WIDTH - ship.width;
		}

		if (started) {
			/*
			 * Enemy Movement
			 */
			for (int i = 0; i < enemies.size(); i++) {
				enemies.get(i).x -= enemySpeed;

			}

			/*
			 * Star Movemnent
			 */
			for (int i = 0; i < stars.size(); i++) {
				stars.get(i).x -= 1;
			}

			/*
			 * If enemy is off screen, take a life away if game is being played,
			 * and remove it from the array, then add a new one outside the
			 * screen
			 * 
			 * If all pipes are cleared, addPipe(2) is called to add a new pipe
			 * outside the bounds of the screen
			 */
			for (int i = 0; i < enemies.size(); i++) {
				Rectangle badGuy = enemies.get(i);
				if (badGuy.x + badGuy.width < 0 && !gameOver) { // enemy is off
																// screen
					if (score > 0) {
						score -= 50;
					} else {
						score = 0;
					}
					enemies.remove(badGuy);
					lives--;
					if (lives == 0) {
						gameOver = true;
					}
					addPipe(1);

				} else if (enemies.isEmpty()) {
					addPipe(2);
				}
			}

			/*
			 * If star is off screen, remove it from the array
			 */
			for (int i = 0; i < stars.size(); i++) {
				Rectangle star = stars.get(i);
				if (star.x + star.width < 0) {
					stars.remove(star);
				}
			}

			/*
			 * If a point giving power pellet is off screen, remove it
			 */
			for (int i = 0; i < powers.size(); i++) {
				Rectangle powerPellet = powers.get(i);
				if (powerPellet.x + powerPellet.width < 0) {
					powers.remove(powerPellet);
				}
			}

			/*
			 * If any enemy intersects the ship (bird), shit gets reset in
			 * gameOver check and one life is taken away
			 * 
			 * powers are cleared from the screen as well
			 */
			for (Rectangle badGuy : enemies) {
				if (badGuy.intersects(ship)) {
					lives--;
					gameOver = true;
					powers.clear();
				}
			}

			/*
			 * If the green pellet is collected, it is removed and 200 is added
			 * to the score
			 */

			for (int i = 0; i <= powers.size() - 1; i++) {
				Rectangle powerPellet = powers.get(i);
				powerPellet.x -= 2;
				if (ship.intersects(powerPellet)) {
					score += 200;
					powers.remove(powerPellet);
				}
			}

			/*
			 * If the blue pellet is collected, it is removed and super shot =
			 * true and it is moved to the bottom with score
			 */

			for (int i = 0; i <= powerShots.size() - 1; i++) {
				Rectangle powerShot = powerShots.get(i);
				powerShot.x -= shotMovement;
				if (ship.intersects(powerShot)) {
					superShot = true;
					shotMovement = 0;
					powerShot.setLocation(WIDTH / 2 - 7, HEIGHT + 50);
				}
			}

			/*
			 * keeps bullets moving (keepShootin is probably not necessary at
			 * all but its how I figured out how to make them move, so I'm
			 * keeping it)
			 */
			if (keepShootin) {
				for (Rectangle bullet : bullets) {
					bullet.x += 10;
					if (bullet.x > WIDTH - 7) {
						bullet.setLocation(-WIDTH, -HEIGHT);
					}
				}
				for (Rectangle superB : superBullets) {
					superB.x += 10;
					if (superB.x > WIDTH - superB.width) {
						superB.setLocation(-WIDTH, -HEIGHT);
					}
				}
			}

			/*
			 * Checks for bullet intersection and removes enemy and bullet
			 * accordingly TODO bullets currently just have to be moved
			 * offscreen instead of out of the array...
			 */
			if (started && bullets.size() > 0) {
				for (int i = 0; i < enemies.size(); i++) {
					for (Rectangle bullet : bullets) {
						if (enemies.get(i).intersects(bullet)) {

							powerUp = rand.nextInt(10);
							if (powerUp == 5) {
								powers.add(new Rectangle(enemies.get(i).x + 50, enemies.get(i).y + 25, 15, 15));
							}

							powerUp = rand.nextInt(15);
							if (powerUp == 7) {
								powerShots.add(new Rectangle(enemies.get(i).x + 70, enemies.get(i).y + 45, 15, 15));
							}

							enemies.remove(i);
							addPipe(1);
							// bullets.remove(bullet);
							bullet.setLocation(-WIDTH * 2, -HEIGHT * 2);
							score += 100;
						}
					}
					if (superBullets.size() > 0) {
						for (Rectangle superBullet : superBullets) {
							if (enemies.get(i).intersects(superBullet)) {
								enemies.remove(i);
								addPipe(1);
								if (superBullet.x + superBullet.width > WIDTH) {
									superBullet.setLocation(1111, 1111);
								}
								score += 100;
							}
						}
					}
				}
			}
			/*
			 * set lives back to three on game over
			 */
			if (lives < 3 && !started) {
				lives = 3;
				powers.clear();
			}

			renderer.repaint();

		}

	}

	public void repaint(Graphics g) throws IOException {

		/*
		 * Background is black
		 */
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		/*
		 * Score Bar is dark gray
		 */
		g.setColor(Color.gray.darker().darker());
		g.fillRect(0, HEIGHT, WIDTH, 200);

		/*
		 * Draws the "lives" string above the life display
		 */
		g.setFont(new Font("Comic Sans MS", 1, 25));
		g.setColor(Color.white);
		g.drawString("lifes", WIDTH / 2 + 200, HEIGHT + 25);

		/*
		 * Draws "lives" as birds depending on lives variable
		 */
		for (int i = 0; i <= lives - 1; i++) {
			BufferedImage image = ImageIO.read(new File("bird.png"));
			g.drawImage(image, WIDTH / 2 + 100 + (50 * (i + 1)), HEIGHT + 50, null);
		}

		/*
		 * Draws each star as added
		 */
		g.setColor(Color.WHITE);
		for (Rectangle star : stars) {
			g.fillRect(star.x, star.y, star.width, star.height);
		}

		/*
		 * Draws the bird during game started = true
		 */

		if (!gameOver && started) {
			BufferedImage image = ImageIO.read(new File("bird.png")); // HEIGHT
																		// 26,
																		// WIDTH
																		// 36
			g.drawImage(image, ship.x, ship.y, null);
		}

		/*
		 * Draws each enemy as added
		 */
		g.setColor(Color.RED);
		for (Rectangle badGuy : enemies) {
			g.fillRect(badGuy.x, badGuy.y, badGuy.width, badGuy.height);
		}

		/*
		 * Draws bullets as added
		 */
		g.setColor(Color.YELLOW);
		for (Rectangle bullet : bullets) {
			g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
		}
		for (Rectangle superBullet : superBullets) {
			g.fillRect(superBullet.x, superBullet.y, superBullet.width, superBullet.height);
		}

		/*
		 * Draws green point pellets as added
		 */
		g.setColor(Color.green.brighter());
		if (powers.size() > 0) {
			for (int i = 0; i <= powers.size() - 1; i++) {
				g.fillRect(powers.get(i).x, powers.get(i).y, powers.get(i).width, powers.get(i).height);
			}
		}

		/*
		 * Draws blue point pellets as added
		 */
		g.setColor(Color.blue.brighter());
		if (powerShots.size() > 0) {
			for (int i = 0; i <= powerShots.size() - 1; i++) {
				g.fillRect(powerShots.get(i).x, powerShots.get(i).y, powerShots.get(i).width, powerShots.get(i).height);
			}
		}

		g.setColor(Color.WHITE);
		g.setFont(new Font("Comic Sans MS", 1, 100));
		/*
		 * Draws opening screen, also asks for difficulty
		 */
		if (!started) {
			g.drawString("graduation", 100, 100);
			g.setFont(new Font("Comic Sans MS", 1, 75));
			g.drawString("select easiness", 100, 200);
			g.setFont(new Font("Comic Sans MS", 1, 50));
			g.drawString("   1: Easy", 100, 300);
			g.drawString("   2: Less Easy", 100, 400);
			g.drawString("   3: Lesser Easier", 100, 500);
		}

		if (gameOver) {
			/*
			 * Supposed to be the ship getting smaller as gameOver is true
			 */
			for (int i = 0; i < 10000000; i++) {
				if (i % 20000 == 0) {
					ship.width -= 2;
					ship.height -= 2;
					g.fillRect(ship.x, ship.y, ship.width, ship.height);
				}
			}

			/*
			 * If the game is over and no lives remain draw final game over
			 * screen and final score otherwise let player know to try again
			 */
			if (lives == 0) {
				g.drawString("WaStASTED", 50, HEIGHT / 2 - 50);
				g.drawString("Score: " + score, 50, HEIGHT / 2 + 75);
			} else {
				g.drawString("Try Again", 100, HEIGHT / 2 - 50);
			}
		}

		/*
		 * Draws the score
		 */
		if (!gameOver && started) {
			g.drawString(Integer.toString(score), 50, HEIGHT + 90);
		}
		/*
		 * Draws the pause string when paused = true;
		 */
		if (paintPaused) {
			g.setFont(new Font("Comic Sans MS", 1, 50));
			g.drawString("stop for hammer time", WIDTH / 2 - 250, HEIGHT / 2 - 50);
		}

	}
	
	private void jump() { 
		if (yMotion > 0){
			yMotion = 0;
		}
		yMotion -= 10;
	}

	private void shoot() {
		int speed = 10;
		int bHeight = 5 - difficulty;
		int bWidth = 10 - difficulty;
		ticks++;
		keepShootin = true;

		if (superShot) {
			shotMovement = 2;
			superShot = false;
			for (int i = 0; i <= powerShots.size() - 1; i++) {
				powerShots.clear();
			}
			superBullets.add(new Rectangle(ship.x, 0, 100, HEIGHT));
		}

		if (started && lives > 0 && !paused) {
			bullets.add(new Rectangle(ship.x + 10, ship.y + 10, bWidth, bHeight));
			for (int b = 0; b < bullets.size(); b++) {
				bullets.get(b).x += speed;
				for (Rectangle enemy : enemies) {
					for (int x = 0; x < enemy.width; x++) {
					}
				}

				if (bullets.get(b).x > WIDTH) {
					bullets.remove(bullets.get(b));
				}
			}
		}

		if (gameOver) {

			stars.clear();
			for (int i = 0; i <= 99; i++) {
				stars.add(new Rectangle(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), 2, 2));
			}
			renderer.repaint();
			powerShots.clear();
			superShot = false;
			superBullets.clear();
			ship = new Rectangle(WIDTH / 2, HEIGHT / 2, 44, 44);
			enemies.clear();

			if (lives == 0) {
				score = 0;
				lives = 3;
			}

			addPipe(1);
			addPipe(1); // these would have been 'true' pipes
			addPipe(1); //
			addPipe(1);

			gameOver = false;
			started = true;
		}

		if (!started) {
			started = true;
			score = 0;
			lives = 3;
		}

	}

	public Rectangle getBounds(Rectangle enemy) {
		return new Rectangle(enemy);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!started) {
			if (e.getKeyCode() == KeyEvent.VK_1) {
				difficulty = 1;
			} else if (e.getKeyCode() == KeyEvent.VK_2) {
				difficulty = 2;
			} else if (e.getKeyCode() == KeyEvent.VK_3) {
				difficulty = 3;
			} else {
				difficulty = 1;
			}
			started = true;
		}

		if (!paused) {
			if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
				up = true;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
				down = true;
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				left = true;

			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				right = true;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

			if (!started) {
				paused = false;
			}
			if (pauseState == 0) {
				pauseState = 1;
				paused = true;
				System.out.println("Paused");
			} else if (pauseState == 1) {
				pauseState = 0;
				paused = false;
				System.out.println("unpaused");
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
			started = true;
			if (started) {
				shoot();
				jump();
			}
		}

		if (!paused) {
			if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
				up = false;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
				down = false;
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				left = false;

			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				right = false;
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}