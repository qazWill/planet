import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Planet extends JFrame {

	/* the current magnification level */
	private double z;
    
    /* the magnification cordinates */
	private double xCenter;
	private double yCenter;
    
    /* rate at which to magnify */
	private double rate;
    
    /* whether or not to display magnification center */
	private boolean showMark;

	/* used for double buffer images */
	Image dbImage;
	Graphics dbg;

	/* the only constructor */
	public Planet() {
    
        /* initial magnification level */
		z = 1.0;
    
        /* initial magnification cordinates */
		xCenter = 400;
		yCenter = 250;
    
        /* magnification rate is constant */
		rate = 0.1;
        
        /* initially, mark is not visible */
		showMark = false;
        
        /* general window properties */
		setTitle("Two Dimensional Planet");
		setSize(800, 600);
		setResizable(false);
		setVisible(true);
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        /* used for user triggered events */
		addKeyListener(new AL());
    }

    /* listens for user key input */
	public class AL extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if (keyCode == e.VK_S) {
				z = (1.0 - rate) * z;
				if (z < 1.0) {
					z = 1.0;
				}
				repaint();
			}
			if (keyCode == e.VK_W) {
				z = (1.0 + rate) * z;
				if (z > 1000000000.00) {
					z = 1000000000.00;
				}
				repaint();
			}
			if (showMark) {
				if (keyCode == e.VK_LEFT) {
					xCenter -= 1 / z;
					repaint();
				}
				if (keyCode == e.VK_RIGHT) {
					xCenter += 1 / z;
					repaint();
				}
				if (keyCode == e.VK_UP) {
					yCenter -= 1 / z;
					repaint();
				}
				if (keyCode == e.VK_DOWN) {
					yCenter += 1 / z;
					repaint();
				}
			}
			if (keyCode == e.VK_H) {
				System.out.println(xCenter + ", " + yCenter);
				showMark = !showMark;
				repaint();
			}
		}
	}

    /* this method draws to the screen */
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		dbg.setFont(new Font("Arial", 0, 12));
		drawPlanetMagnified(z, 400, 300, 50, dbg);
		if (showMark) {
			dbg.setColor(Color.RED);
			dbg.fillRect((int)xCenter - 2, (int)yCenter - 2, 5, 5);
		}
		double cm = 10 * (500000000 / z);
		String string;
		if (cm < 100) {
			dbg.drawString(String.format("%40.2f", cm) + " centimeters", 10, 485);
		} else if (cm < 100000) {
			dbg.drawString(String.format("%40.2f", (cm / 100)) + " meters", 10, 485);
		} else {
			dbg.drawString(String.format("%40.2f", (cm / 100000)) + " kilometers", 10, 485);
		}
		dbg.drawLine(100, 500, 700, 500);
		dbg.drawLine(100, 490, 100, 510);
		dbg.drawLine(700, 490, 700, 510);
		g.drawImage(dbImage, 0, 0, this);
		z *= 1.005;
		repaint();
	}

    /* 
    This method draws a planet of a given radius at any given 
    magnification for any cordinates.  It uses the drawLine
    method to accomplish this.
    
    double zoom - magnification level to draw with
    double xPlanet - x cordinate of planet center
    double yPlanet - y cordinate of planet center
    double radius - the radius of the planet
    Graphics g - the object used for drawing
    */
	public void drawPlanetMagnified(double zoom, double xPlanet, double yPlanet, double radius, Graphics g) {
		double range = 0.01;
		xPlanet = xCenter - zoom * (xCenter - xPlanet);
		yPlanet = yCenter - zoom * (yCenter - yPlanet);
		radius = zoom * radius;
		drawLine(0, range, xPlanet, yPlanet - radius, xPlanet + radius, yPlanet, xPlanet, yPlanet, g);
		drawLine(0, range, xPlanet + radius, yPlanet, xPlanet, yPlanet + radius, xPlanet, yPlanet, g);
		drawLine(0, range, xPlanet, yPlanet + radius, xPlanet - radius, yPlanet, xPlanet, yPlanet, g);
		drawLine(0, range, xPlanet - radius, yPlanet, xPlanet, yPlanet - radius, xPlanet, yPlanet, g);
	}
    
    
	/*
    This method draws the individual lines of the planet's surface.
    Magnified lines are displaced until they achieve a certain degree
    of detail.  The diamond-square algorithm is modified to displace
    the lines about a circle instead of a straight line.
    
    double depth - recursive level
    double range - range at which new radius can be displaced
    double xA - x component of first point
    double yA - y component of first point
    double xB - x component of second point
    double yB - y component of second point
    double xP - x component of planet's center
    double yP - y component of planet's center
    Graphics g - object used for drawing
    */
	public void drawLine(double depth, double range, double xA, double yA, double xB, double yB, double xP, double yP, Graphics g) {

        /* tests for desired detail */
		if (Math.sqrt(Math.pow(xB - xA, 2) + Math.pow(yB - yA, 2)) <= 2) {
        
            /* draws a line from A to B */
			g.drawLine((int)xA, (int)yA, (int)xB, (int)yB);
            
        /* if more detail is needed */
		} else {

			/* finds average distance from center of the two points */
			double averageRadius = Math.sqrt(Math.pow(xA - xP, 2) + Math.pow(yA - yP, 2));
			averageRadius += Math.sqrt(Math.pow(xB - xP, 2) + Math.pow(yB - yP, 2));
			averageRadius /= 2;
			
			/* finds midpoint of A and B */
			double xM = (xA + xB) / 2.0;
			double yM = (yA + yB) / 2.0;

			/* finds unit vector for the displacement vector given by M - P */
			double distance = Math.sqrt(Math.pow(xM - xP, 2) + Math.pow(yM - yP, 2));
			double xN = (xM - xP) / distance;
			double yN = (yM - yP) / distance;
			
			/* displaces the new height value */
			double percent = noise(xN, yN);
			double displacedRadius = averageRadius + percent * range * averageRadius;
			double xNew = xP + (displacedRadius * xN);
			double yNew = yP + (displacedRadius * yN);
			
            /* tests to ensure line is visible */
			if (inside(xA, yA, xB, yB)) {
            
                /* subdivides original line into two others */
				drawLine(depth + 1, range / 1.75, xA, yA, xNew, yNew, xP, yP, g);
				drawLine(depth + 1, range / 1.75, xNew, yNew, xB, yB, xP, yP, g);
			}
		}
	}

    /*
    This method determines if a line could be whithin view or
    at least close.  It uses a bounding box.  Admittantly, this method
    would need to be changed if I did not magnify toward the same point
    that I have every time.
    
    double xA - x component of first point of line
    double yA - y component of first point of line
    double xB - x component of second point of line
    double yB - y component of second point of line
    */
	public boolean inside(double xA, double yA, double xB, double yB) {
		double xMin = xA;
		if (xB < xMin) {
			xMin = xB;
		}

		double xMax = xA;
		if (xB > xMax) {
			xMax = xB;
		}

		double yMax = yA;
		if (yB > yMax) {
			yMax = yB;
		}

		double yMin = yA;
		if (yB < yMin) {
			yMin = yB;
		}

		if (xMax < 0 - 800 || xMin > 800 + 800|| yMax < 0 - 800|| yMin > 600 + 800) {
			return false;
		}
		return true;
	}

    /*
    This function produces a somewhat random value that can be 
    repeatedly reproduced for a unique pair of x and y values.
    
    double x - x value
    double y - y value
    */
	public double noise(double x, double y) {
		x -= 2673.582017;
		y += 954.1399023;
		return Math.sin(x * y * 8893017.727);
	}

    /* entry point of program */
	public static void main(String[] args) {
    
        /* creates a Planet object */
		Planet planet = new Planet();
	}
}





