import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

public class WavePanel extends JPanel {

	// debug
	boolean debug = false;

	// screen
	boolean help = false;
	int screenWidth, screenHeight;
	boolean invert = false;
	boolean grid = false;
	boolean hide = false;

	// camera
	double speed = 0.0;
	double regSpeed = 0.0;
	boolean follow = false;
	double timeLoc = 0.0; // ms

	// engine
	final long timeStart = System.currentTimeMillis();
	long frame = 0;
	long frameTime = 0; // ms
	double targetFPS = 60.0;
	double currentFPS = 0.0;
	double targetTime = 1000 / targetFPS; // ms
	long periodStart, periodFinish; // ms
	long currentTotalTime = 0; // ms
	boolean pause = false;
	double timeMult = 1.0;

	// constants
	double period = 2 * Math.PI;

	// waves
	ArrayList<Wave> waves = new ArrayList<Wave>();
	int scenario = 1;
	double precision = 1.0;
	double hStretch = 128.0;

	// inputs
	private KeyHandler keyHandler; // key inputs
	private ArrayList<Integer> heldKeys = new ArrayList<Integer>();

	public WavePanel(double screenWidth, double screenHeight) {
		// screen
		this.screenWidth = (int) screenWidth;
		this.screenHeight = (int) screenHeight;

		// camera
		speed = hStretch / targetFPS;

		// waves
		fullReset();
		/*
		if (scenario == 1) {
			waves.add(new Wave(Color.RED, 50.0, true, period));
			waves.add(new Wave(Color.BLUE, 100.0, true, period));
		} else if (scenario == 2) {
			waves.add(new Wave(Color.RED, 100.0, true, period));
			waves.add(new Wave(Color.YELLOW, 100.0, true, period * 0.5));
			waves.add(new Wave(Color.BLUE, 100.0, true, period * 0.25));
		}
		*/

		// inputs
		this.keyHandler = new KeyHandler();
		addKeyListener(this.keyHandler);
		setFocusable(true);
	}

	public void paintComponent(Graphics graphics) {
		// basics
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		long frameStart = System.currentTimeMillis();

		// background
		g.setColor(Color.BLACK);
		if (invert == true) {
			g.setColor(Color.WHITE);
		}
		g.fillRect(0, 0, screenWidth, screenHeight);

		// camera
		int direction = 0;
		for (int key : heldKeys) {
			if (key == KeyEvent.VK_A) {
				direction += -1;
			} else if (key == KeyEvent.VK_D) {
				direction += 1;
			}
		}
		// move
		if (heldKeys.indexOf(KeyEvent.VK_SHIFT) != -1) {
			timeLoc += timeMult * direction * speed * 2;
		} else {
			timeLoc += timeMult * direction * speed;
		}
		if (timeLoc < 0)
			timeLoc = 0;

		// grid
		g.translate(0, screenHeight / 2);
		if (grid == true) {
			g.setColor(Color.WHITE);
			if (invert == true) {
				g.setColor(Color.BLACK);
			}
			// time
			g.drawLine(0, 0, screenWidth, 0);
			double secondMark = (int) (60 * targetTime / 1000 * hStretch);
			int numMarks = (int) ((screenWidth / secondMark) + 1 + (timeLoc / secondMark));
			for (int i = 0; i < numMarks; i++) {
				int mark = (int) (60 * targetTime / 1000 * hStretch * i - timeLoc);
				g.drawLine(mark, 10, mark, -10);
			}
			// amplitude
			g.drawLine(screenWidth / 2, screenHeight / 2, screenWidth / 2, screenHeight / -2);
			numMarks = (int) (screenHeight / 100 / 2) + 2;
			for (int i = 0; i < numMarks; i++) {
				g.drawLine(screenWidth / 2 - 10, 100 * i, screenWidth / 2 + 10, 100 * i);
				g.drawLine(screenWidth / 2 - 10, -100 * i, screenWidth / 2 + 10, -100 * i);
			}
		}

		// waves
		if (pause == false) {
			for (Wave wave : waves) {
				for (int i = 0; i < timeMult; i++) {
					wave.genPoint((currentTotalTime + targetTime * i) / 1000.0);
				}
			}
		}

		// drawing
		// each
		if (hide == false) {
			for (Wave wave : waves) {
				g.setColor(wave.getColor());
				ArrayList<double[]> points = wave.getPoints();
				for (int i = 0; i < points.size() - 1; i++) {
					int x1 = (int) (points.get(i)[0] * hStretch - timeLoc);
					int y1 = (int) (points.get(i)[1]);
					int x2 = (int) (points.get(i + 1)[0] * hStretch - timeLoc);
					int y2 = (int) (points.get(i + 1)[1]);
					g.drawLine(x1, y1 * -1, x2, y2 * -1);
				}
			}
		}
		// superposition
		g.setColor(Color.WHITE);
		if (invert == true) {
			g.setColor(Color.BLACK);
		}
		for (int i = 0; i < waves.get(0).getPoints().size() - 1; i++) {
			int x1 = 0;
			int y1 = 0;
			int x2 = 0;
			int y2 = 0;
			for (Wave wave : waves) {
				ArrayList<double[]> points = wave.getPoints();
				x1 = (int) (points.get(i)[0] * hStretch - timeLoc);
				y1 += (int) (points.get(i)[1]);
				x2 = (int) (points.get(i + 1)[0] * hStretch - timeLoc);
				y2 += (int) (points.get(i + 1)[1]);
			}
			g.drawLine(x1, y1 * -1, x2, y2 * -1);
		}

		// gui
		g.translate(0, screenHeight / -2);
		g.setColor(Color.WHITE);
		if (invert == true) {
			g.setColor(Color.BLACK);
		}
		g.setFont(new Font("Dialog", Font.PLAIN, 14)); // title font
		g.drawString("Wave Analyzer", 10, 20); // program title
		g.setFont(new Font("Dialog", Font.PLAIN, 10)); // subtitle font
		g.drawString("v.3", 110, 20); // program version
		g.drawString("by Jason Kim", 10, 35); // program title
		int start = 60;
		g.setFont(new Font("Dialog", Font.PLAIN, 12)); // regular font

		// help
		if (help == false && debug == false) {
			g.drawString("Press \"H\" for controls.", 10, start);
		} else if (help == true && debug == false) {
			ArrayList<String> controlsList = new ArrayList<String>();
			controlsList.add("Controls:");
			controlsList.add("");
			controlsList.add("Esc - Quit");
			controlsList.add("");
			controlsList.add("Space - Pause");
			controlsList.add("Period - Increase Time Speed");
			controlsList.add("Comma - Decrease Time Speed");
			controlsList.add("");
			controlsList.add("F - Follow");
			controlsList.add("A - Move Left");
			controlsList.add("D - Move Right");
			controlsList.add("Shift - Increase Move Speed");
			controlsList.add("");
			controlsList.add("Caps Lock - Invert");
			controlsList.add("G - Show/Hide Grid");
			controlsList.add("\\ - Show/Hide Waves");
			controlsList.add("E - Increase Horizontal Stretch");
			controlsList.add("Q - Decrease Horizontal Stretch");
			controlsList.add("");
			controlsList.add("R - Reset");
			controlsList.add("1 - Constructive Interference");
			controlsList.add("2 - Destructive Interference");
			controlsList.add("3 - Superposition Demonstration");
			controlsList.add("4 - Random");
			for (int i = 0; i < controlsList.size(); i++) {
				g.drawString(controlsList.get(i), 10, start + 20 * i);
			}
		}

		// debug
		if (debug == true) {
			ArrayList<String> debugMenu = new ArrayList<String>();
			debugMenu.add("Debug Menu:");
			debugMenu.add("");
			debugMenu.add("Frame: " + frame);
			debugMenu.add("Frame Time (ms): " + frameTime);
			debugMenu.add("Target FPS: " + targetFPS);
			debugMenu.add("Current FPS: " + (Math.round(currentFPS * 10) / 10.0));
			debugMenu.add("Current Total Time: " + currentTotalTime);
			debugMenu.add("Paused: " + pause);
			debugMenu.add("Time Multiplier: " + timeMult);
			debugMenu.add("");
			debugMenu.add("Direction: " + direction);
			debugMenu.add("Speed: " + speed);
			debugMenu.add("Time Location: " + timeLoc);
			debugMenu.add("");
			debugMenu.add("Wave Count: " + waves.size());
			debugMenu.add("Scenario: " + scenario);
			debugMenu.add("Precision: " + precision);
			debugMenu.add("Hor. Stretch: " + hStretch);
			for (int i = 0; i < debugMenu.size(); i++) {
				g.drawString(debugMenu.get(i), 10, start + 20 * i);
			}
		}

		// fps stabilization
		if ((System.currentTimeMillis() - frameStart) < targetTime) {
			try {
				TimeUnit.MILLISECONDS.sleep((long) (targetTime - (System.currentTimeMillis() - frameStart)));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// finish frame
		long frameFinish = System.currentTimeMillis();
		frameTime = frameFinish - frameStart;
		currentFPS = 1.0 / (frameTime / 1000.0);
		if (pause == false)
			currentTotalTime += targetTime * timeMult;

		// next frame
		frame++;
		repaint();
	}

	public void reset() {
		timeLoc = 0.0;
		pause = false;
		timeMult = 1.0;
		currentTotalTime = 0;
		for (Wave wave : waves) {
			wave.reset();
		}
	}

	public void fullReset() {
		reset();
		hStretch = 128.0;
		waves.clear();
		if (scenario == 1) {
			waves.add(new Wave(Color.RED, 50.0, true, period, 0.0, 0.0));
			waves.add(new Wave(Color.BLUE, 100.0, true, period, 0.0, 0.0));
		} else if (scenario == 2) {
			waves.add(new Wave(Color.RED, -100.0, true, period, 0.0, 0.0));
			waves.add(new Wave(Color.BLUE, 100.0, true, period, 0.0, 0.0));
		} else if (scenario == 3) {
			waves.add(new Wave(Color.RED, 100.0, true, period, 0.0, 0.0));
			waves.add(new Wave(Color.YELLOW, 100.0, true, period * 0.5, 0.0, 0.0));
			waves.add(new Wave(Color.BLUE, 100.0, true, period * 0.25, 0.0, 0.0));
		} else if (scenario == 4) {
			int numWaves = (int) (Math.random() * 8) + 2;
			for (int i = 0; i < numWaves; i++) {
				waves.add(new Wave(new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)), Math.random() * 50 + 50, (int) (Math.random() * 2) == 0, period * Math.random(), Math.random() * period, Math.random() * 50 - 25.0));
			}
		}
	}

	// key inputs
	class KeyHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			// time
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				pause = !pause;
				if (pause == false) {
					repaint();
				}
			} else if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
				timeMult *= 2;
			} else if (e.getKeyCode() == KeyEvent.VK_COMMA) {
				timeMult /= 2;
			}
			// view
			else if (e.getKeyCode() == KeyEvent.VK_F3) {
				debug = !debug;
			} else if (e.getKeyCode() == KeyEvent.VK_H && debug == false) {
				help = !help;
			} else if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
				invert = !invert;
			} else if (e.getKeyCode() == KeyEvent.VK_G) {
				grid = !grid;
			} else if (e.getKeyCode() == KeyEvent.VK_BACK_SLASH) {
				hide = !hide;
			}
			// move
			else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_SHIFT) {
				if (heldKeys.indexOf(e.getKeyCode()) == -1)
					heldKeys.add(e.getKeyCode());
			} else if (e.getKeyCode() == KeyEvent.VK_F) {
				follow = !follow;
			}
			// stretch
			else if (e.getKeyCode() == KeyEvent.VK_E) {
				hStretch *= 2;
				timeLoc *= 2;
			} else if (e.getKeyCode() == KeyEvent.VK_Q) {
				hStretch /= 2;
				timeLoc /= 2;
			}
		}

		public void keyReleased(KeyEvent e) {
			// quit
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
			// move
			else if (heldKeys.indexOf(e.getKeyCode()) != -1) {
				heldKeys.remove(heldKeys.indexOf(e.getKeyCode()));
			}
			// reset
			else if (e.getKeyCode() == KeyEvent.VK_R) {
				reset();
			}
			// scenarios
			else if (e.getKeyCode() == KeyEvent.VK_1) {
				scenario = 1;
				fullReset();
			} else if (e.getKeyCode() == KeyEvent.VK_2) {
				scenario = 2;
				fullReset();
			} else if (e.getKeyCode() == KeyEvent.VK_3) {
				scenario = 3;
				fullReset();
			} else if (e.getKeyCode() == KeyEvent.VK_4) {
				scenario = 4;
				fullReset();
			}
		}
	}

}
