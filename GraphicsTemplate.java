package slicey;

import javax.swing.*;

import PersonalProjects.Game3D.MyKeyListener;
import PersonalProjects.Game3D.MyMouseListener;
import PersonalProjects.Game3D.MyMouseMotionListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;

public class GraphicsTemplate extends JPanel {
	//basic information about the program
	public static int width = 720;
	public static int height = 720;
	public static int frameRate = 10; // ms per frame
	public static int currentFrame = 0;
	public static Color backgroundColor = Color.white;
	
	//other instance variables
	public static Area shape;
	public static Area cutShape;
	public static Polygon goal = new Polygon();
	
	public static Point point = new Point(width / 2, height / 2);
	public static Point ghostPoint = new Point(width / 2, height / 2);
	public static Point mouse = new Point();
	
	public static int pointRadius = 5;
	public static int shapeSize;
	public static double score = 0;
	
	public static boolean pointSelected = false;
	public static boolean mouseDown = false;
	
	public static void initialize() {
		//initialize shape
		Polygon s = new Polygon();
		s.addPoint(100, 100);
		s.addPoint(100, 600);
		s.addPoint(600, 600);
		s.addPoint(600, 100);
		shapeSize = Math.max((int)s.getBounds().getWidth(), (int)s.getBounds().getHeight());
		shape = new Area(s);
		
		//initialize goal
		goal.addPoint(350, 100);
		goal.addPoint(225, 300);
		goal.addPoint(100, 300);
		goal.addPoint(225, 500);
		goal.addPoint(150, 600);
		goal.addPoint(350, 500);
		goal.addPoint(450, 600);
		goal.addPoint(375, 500);
		goal.addPoint(600, 300);
		goal.addPoint(375, 300);
	}
	

	public static void mainLoop() {
		//System.out.println(pointSelected);
		if(pointSelected) {
			point.move(mouse.x, mouse.y);
		}
		if(!mouseDown) {
			ghostPoint.move(mouse.x, mouse.y);
		}
		score = overlapScore(shape, new Area(goal));
		if(score > .95)
			score = 1;
	}
	
	public static void paintLoop(Graphics2D g) {
		//shape
		g.setColor(new Color(120, 60, 60));
		g.fill(shape);
		
		//ghost line
		g.setColor(Color.gray);
		Point lV = new Point(ghostPoint.x - point.x, ghostPoint.y - point.y);
		double length = Math.sqrt(lV.x * lV.x + lV.y * lV.y);
		double x = (double)lV.x / length;
		double y = (double)lV.y / length;
		x *= 4 * shapeSize;
		y *= 4 * shapeSize;
		if(!(Math.abs(point.x - ghostPoint.x) < pointRadius * 2 && Math.abs(point.y - ghostPoint.y) < pointRadius * 2) && !pointSelected)
			g.drawLine(point.x, point.y, point.x + (int)x, point.y + (int)y);
		
		//cut line
		if(mouseDown && !pointSelected) {
			//cut line
			g.setColor(Color.black);
			lV = new Point(mouse.x - point.x, mouse.y - point.y);
			length = Math.sqrt(lV.x * lV.x + lV.y * lV.y);
			double cutx = (double)lV.x / length;
			double cuty = (double)lV.y / length;
			cutx *= 4 * shapeSize;
			cuty *= 4 * shapeSize;
			g.drawLine(point.x, point.y, point.x + (int)cutx, point.y + (int)cuty);
			
			//cut shape
			g.setColor(backgroundColor);
			Polygon cut = new Polygon();
			cut.addPoint(point.x, point.y);
			cut.addPoint(point.x + (int)cutx, point.y + (int)cuty);
			cut.addPoint(point.x + (int)x, point.y + (int)y);
			cutShape = new Area(cut);
			g.fill(cutShape);
			
		}
		
		//point
		g.setColor(Color.black);
		g.fillOval(point.x - pointRadius, point.y - pointRadius, 2 * pointRadius, 2 * pointRadius);
		
		//goal
		g.setColor(Color.black);
		g.drawPolygon(goal);
		
		//accuracy
		if(score < .40) {
			g.setColor(Color.red);
		}else if(score < .85) {
			g.setColor(new Color(200, 200, 100));
		}else {
			g.setColor(Color.green);
		}
		g.setFont(new Font("Franklin Gothic", Font.PLAIN, 25));
		g.drawString((int)(score * 100) + "%", width / 2 - 40, 50);
		
	}
	
	//thanks chatGPT
	// Compute area of an Area object using shoelace method
    private static double computeArea(Area a) {
        PathIterator pi = a.getPathIterator(null);
        double[] coords = new double[6];
        double area = 0.0;

        double startX = 0, startY = 0;
        double lastX = 0, lastY = 0;

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    startX = lastX = coords[0];
                    startY = lastY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    area += (lastX * coords[1] - coords[0] * lastY);
                    lastX = coords[0];
                    lastY = coords[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    area += (lastX * startY - startX * lastY);
                    break;
            }
            pi.next();
        }
        return Math.abs(area) / 2.0;
    }

    // Overlap ratio (Jaccard index) between shape and goal
    public static double overlapScore(Area shape, Area goal) {
        // Intersection
        Area intersection = new Area(shape);
        intersection.intersect(goal);

        // Union
        Area union = new Area(shape);
        union.add(goal);

        double areaIntersection = computeArea(intersection);
        double areaUnion = computeArea(union);

        if (areaUnion == 0) return 0.0; // avoid div by zero
        return areaIntersection / areaUnion;
    }

	//------------------------------------------- o -------------------------------------------------
	
	/**
	 * These below 2 methods are setup and do not need to be changed;
	 * mainLoop() and paintLoop() will be called every frame. Frame rate is determined above in the 
	 * static class variables. However, the frame rate corresponds to something different than
	 * normal in this program: frameRate describes how many milliseconds the system waits 
	 * before each call of mainLoop() and paintLoop(), so if either of these methods takes 
	 * a non-insignificant time to run, the frame rate of the program will not correspond 
	 * to the frameRate variable
	 */
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        //antialiasing off means hard edges, antialiasing on means soft edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        //background
        g.setColor(backgroundColor);
        g.fillRect(0,0,2000,2000);
        
        //calling paintLoop()
        paintLoop(g2d);
    }
    
    public GraphicsTemplate() {
		KeyListener d = new MyKeyListener();
		addKeyListener(d);
		MouseListener mML = new MyMouseListener();
		addMouseListener(mML);
		MouseMotionListener mMML = new MyMouseMotionListener();
		addMouseMotionListener(mMML);
		setFocusable(true);
	}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Java 2D Graphics Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GraphicsTemplate());
        frame.setSize(width, height);
        frame.setVisible(true);
        initialize();
        while(true){
        	// currentFrame describes how many frames have occurred throughout the duration of run time
            currentFrame ++;
            frame.repaint();
            mainLoop();
            try{
                Thread.sleep(frameRate);
            }
            catch(Exception e){
                System.out.println("Error!");
            }
        }
    }
    
    public class MyKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			// this is a dumb method, use 'keyPressed(KeyEvent e)' unless you know what
			// you're doing
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
		}
	}

	public class MyMouseListener implements MouseListener {
		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(!pointSelected) {
				shape.subtract(cutShape);
			}
			pointSelected = false;
			mouseDown = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(Math.abs(e.getX() - point.x) <= 2 * pointRadius && Math.abs(e.getY() - point.y) <= 2 * pointRadius) {
				pointSelected = true;
			}
			mouseDown = true;
		}

		@Override
		public void mouseClicked(MouseEvent e) {

		}
	}

	public class MyMouseMotionListener implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			mouse.move(e.getX(), e.getY());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouse.move(e.getX(), e.getY());
		}
	}
}