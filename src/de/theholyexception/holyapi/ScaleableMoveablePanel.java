package de.theholyexception.holyapi;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScaleableMoveablePanel extends JPanel {
	
	private static final long serialVersionUID = 4805636113671727269L;
	
	private double minZoom = 0.01;
	private double maxZoom = 20000000.0;
	
	private double scale = 1.0d;
	private int zoomX = 0;
	private int zoomY = 0;
	
	private JFrame window;
	private MouseListener mouseListener;
	private Point mouseLocation = new Point(0,0);
	private long lastDraw = 0;
	private boolean leftClicked    = false;
	private boolean middleClicked  = false;
	private boolean rightClicked   = false;
	public ScaleableMoveablePanel(JFrame window) {
		this.window = window;
		
		mouseListener = new MouseListener(this);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	//region Setter
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public void setZoomX(int zoomX) {
		this.zoomX = zoomX;
	}
	
	public void setZoomY(int zoomY) {
		this.zoomY = zoomY;
	}
	//endregion Setter
	
	//region Getter
	public double getScale() {
		return scale;
	}
	
	public int getZoomX() {
		return zoomX;
	}
	
	public int getZoomY() {
		return zoomY;
	}
	
	public int[] getPositionOnMap(long x, long y) {
		int[] l = new int[2];
		l[0] = (int)Math.floor((zoomX + x) / scale);
		l[1] = (int)Math.floor((zoomY + y) / scale);
		return l;
	}
	
	public int[] getPositionOnMap(double x, double y) {
		int[] l = new int[2];
		l[0] = (int)Math.floor((zoomX + x) / scale);
		l[1] = (int)Math.floor((zoomY + y) / scale);
		return l;
	}
	
	public int[] getPositionOnMap(Point point) {
		int[] l = new int[2];
		l[0] = (int)Math.floor((zoomX + point.x) / scale);
		l[1] = (int)Math.floor((zoomY + point.y) / scale);
		return l;
	}
	//endregion Getter
		
	//region Mouseevents
	public void onMouseMoved(MouseEvent event) {
		mouseLocation = event.getPoint();
	}
	
	public void onMouseDraggedLeft(MouseEvent event) {
	}
	
	public void onMouseDraggedMiddle(MouseEvent event) {
	}

	public void onMouseDraggedRight(MouseEvent event) {
		int x = (int)(mouseLocation.getX() - event.getPoint().getX());
		int y = (int)(mouseLocation.getY() - event.getPoint().getY());
		
		zoomX += x;
		zoomY += y;
		
		mouseLocation = event.getPoint();
		if (System.currentTimeMillis() - lastDraw > 20) {
			window.repaint();
			lastDraw = System.currentTimeMillis();
		}
	}
	
	public void onMousePressed(MouseEvent event) {
		switch (event.getButton()) {
		case MouseEvent.BUTTON1: leftClicked   = true; break;
		case MouseEvent.BUTTON2: middleClicked = true; break;
		case MouseEvent.BUTTON3: rightClicked  = true; break;		
		}
	}
	
	public void onMouseReleased(MouseEvent event) {
		switch (event.getButton()) {
		case MouseEvent.BUTTON1: leftClicked   = false; break;
		case MouseEvent.BUTTON2: middleClicked = false; break;
		case MouseEvent.BUTTON3: rightClicked  = false; break;		
		}
	}
	
	public void onMouseWheel(MouseWheelEvent event) {
		double oldScale = scale;
		if (event.getWheelRotation() < 0) {
			scale += 0.1*scale;
			if (scale > maxZoom) scale = maxZoom;
		} else {
			scale -= 0.1*scale;
			if (scale < minZoom) scale = minZoom;
		}
		
		zoomX = (int)((zoomX + event.getX()) / oldScale*scale) - event.getX();
		zoomY = (int)((zoomY + event.getY()) / oldScale*scale) - event.getY();
		
		if (System.currentTimeMillis() - lastDraw > 20) {
			window.repaint();
			lastDraw = System.currentTimeMillis();
		}
	}
	//endregion Mouseevents

	class MouseListener extends MouseAdapter {
		
		private ScaleableMoveablePanel panel;
		
		public MouseListener(ScaleableMoveablePanel panel) {
			this.panel = panel;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			panel.onMouseMoved(e);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (leftClicked) onMouseDraggedLeft(e);
			if (middleClicked) onMouseDraggedMiddle(e);
			if (rightClicked) onMouseDraggedRight(e);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			panel.onMousePressed(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			panel.onMouseReleased(e);
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			panel.onMouseWheel(e);
		}
		
	}
	
}


