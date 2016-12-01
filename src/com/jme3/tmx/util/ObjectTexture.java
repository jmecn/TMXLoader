package com.jme3.tmx.util;

import java.util.List;
import java.util.logging.Logger;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.tmx.core.ObjectNode;
import com.jme3.util.BufferUtils;

/**
 * This is an util for {@link com.jme3.tmx.core.ObjectNode}.
 * 
 * It can draw an image for ellipse, polygon, polyline.
 * 
 * @author yanmaoyuan
 *
 */
public class ObjectTexture extends Texture2D {
	
	static Logger logger = Logger.getLogger(ObjectTexture.class.getName());

	/**
	 * The objectNode should be drawn
	 */
	private ObjectNode obj;
	
	/**
	 * Image width and height
	 */
	private int width, height;
	/**
	 * The width to paint the line at (in pixels), default 2f
	 */
	private float lineWidth = 1f;
	
	/**
	 * an image, Format.RGBA8
	 */
	private Image image;
	private ImageRaster imageRaster;
	
	/**
	 * paint color, default White.
	 */
	private ColorRGBA color = new ColorRGBA(1f, 1f, 1f, 1f);

	/**
	 * Construct a texture with given ObjectNode
	 * @param object
	 */
	public ObjectTexture(ObjectNode object) {
		super();
		
		this.obj = object;

		// may the image a little larger
		width = round(obj.getWidth() * 4);
		height = round(obj.getHeight() * 4);
		
		if (width == 0 && height == 0) {
			throw new IllegalArgumentException("ObjectNode size is zero.");
		}
		
		createImage(width, height);
		
		paint();
	}
	
	/**
	 * create an {@link com.jme3.texture.Image} with given size
	 * 
	 * @param width
	 * @param height
	 */
	private void createImage(int width, int height) {
		if (width == 0 || height == 0) {
			throw new IllegalArgumentException("Image size can not be zero.");
		}
		
		int len = width * height * 4;
		image = new Image(Image.Format.RGBA8, width, height,
				BufferUtils.createByteBuffer(len), null,
				ColorSpace.sRGB);
		imageRaster = ImageRaster.create(image);
		
		setImage(image);
		setWrap(WrapMode.Repeat);
		setMagFilter(MagFilter.Bilinear);
        setMinFilter(MinFilter.Trilinear);
	}
	
	private void paint() {
		switch (obj.getObjectType()) {
		case Polygon:
		case Polyline:
			paintPolyline();
			break;
		case Rectangle:
			paintRect();
			break;
		case Ellipse:
			paintEllipse();
			break;
		case Tile:
			width = round(obj.getWidth());
			height = round(obj.getHeight());
			break;
		case Image:
			// do I need this one?
			break;
		}
	}
	
	@Override
	public Texture2D clone() {
		Texture2D tex = new Texture2D();
		tex.setImage(image);
		tex.setWrap(WrapMode.Repeat);
		setMagFilter(MagFilter.Nearest);
        setMinFilter(MinFilter.NearestNoMipMaps);
		return tex;
	}

	/**
	 * Paints the specified area within the Image with the specified color. The
	 * rectangle will be clipped to the bounds of the Image.
	 * 
	 * @param startX
	 *            The X coordinate of the bottom left corner of the rectangle
	 * @param startY
	 *            The Y coordinate of the bottom left corner of the rectangle
	 * @param width
	 *            The width of the rectangle
	 * @param height
	 *            The height of the rectangle
	 * @param color
	 *            The color to paint the area with.
	 */
	private final void paintRect(int startX, int startY, int width, int height) {
		int endX = startX + width;
		int endY = startY + height;
		if (startX < 0) {
			startX = 0;
		}
		if (startY < 0) {
			startY = 0;
		}
		if (endX > imageRaster.getWidth()) {
			endX = imageRaster.getWidth();
		}
		if (endY > imageRaster.getHeight()) {
			endY = imageRaster.getHeight();
		}

		// fill it with light gray
		float alpha = color.a;
		color.a *= 0.25f;
		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				paintPixel(x, y);
			}
		}
		
		color.a = alpha;
		for (int y = startY; y <= endY; y++) {
			paintPixel(startX, y);
			paintPixel(endX, y);
		}
		
		for (int x = startX; x <= endX; x++) {
			paintPixel(x, startY);
			paintPixel(x, endY);
		}
		
	}

	/**
	 * Paints a straight line between two points on the ImageRaster. This is not
	 * clipped so all points must be greater than width/2 from the edges of the
	 * ImageRaster. It copes with lines in any direction including negative x
	 * and y.
	 * 
	 * The edges of the line are anti-aliased and it copes with non-integer
	 * (even smaller than 1 although the results may not be ideal) widths.
	 * Currently there is no special handling of the end of the line and it just
	 * just cut off either vertically or horizontally so wide lines may need
	 * some work at the ends to tidy them up.
	 * 
	 * @param x1
	 *            The x co-ordinate of the start of the line
	 * @param y1
	 *            The y co-ordinate of the start of the line
	 * @param x2
	 *            The x co-ordinate of the end of the line
	 * @param y2
	 *            The y co-ordinate of the end of the line
	 * @param width
	 *            The width to paint the line at (in pixels)
	 */
	private void paintLine(int x1, int y1, int x2, int y2) {
		float alpha = color.a;
		int xWidth = x2 - x1;
		int yHeight = y2 - y1;

		// Is the line more horizontal or more vertical?
		if (FastMath.abs(xWidth) > FastMath.abs(yHeight)) {
			float yStep = (float) yHeight / xWidth;
			// Trig to work out the angle needed then rotate the width
			// accordingly
			// Using atan not atan2 as we don't actually need the sign of the
			// result
			float angle = FastMath.HALF_PI + FastMath.atan(yStep);
			float rotatedHeight = lineWidth / FastMath.sin(angle);

			int xStep = xWidth < 0 ? -1 : 1;
			float y = y1 - rotatedHeight * 0.5f;

			for (int x = x1; x != x2; x += xStep, y += yStep) {

				// Draw bottom pixel at correct opacity
				color.a = alpha * (1 - (y - (int) y));
				paintPixel(x, (int) y);

				float remainder = y + rotatedHeight;

				// Draw center full opacity pixels
				for (int h = (int) y + 1; h < remainder - 1; h++) {
					paintPixel(x, h);
				}

				// Narrow lines (width < 1) may draw on same pixel twice which
				// makes the
				// line look a little dotty but we ned to handle that case
				// somehow...
				color.a = alpha * (remainder - (int) remainder);
				paintPixel(x, (int) (remainder));
			}
		} else {

			float xStep = (float) xWidth / yHeight;
			// Trig to work out the angle needed then rotate the width
			// accordingly
			// Using atan not atan2 as we don't actually need the sign of the
			// result
			float rotatedWidth = lineWidth / FastMath.sin(FastMath.HALF_PI + FastMath.atan(xStep));
			int yStep = yHeight < 0 ? -1 : 1;
			float x = x1 - rotatedWidth * 0.5f;

			for (int y = y1; y != y2; x += xStep, y += yStep) {

				// Draw bottom pixel at correct opacity
				color.a = alpha * (1 - (x - (int) x));
				paintPixel((int) x, y);

				float remainder = x + rotatedWidth;

				// Draw center full opacity pixels
				for (int w = (int) x + 1; w < remainder - 1; w++) {
					paintPixel(w, y);
				}

				// Narrow lines (width < 1) may draw on same pixel twice which
				// makes the
				// line look a little dotty but we ned to handle that case
				// somehow...
				color.a = alpha * (remainder - (int) remainder);
				paintPixel((int) (remainder), y);
			}

		}
	}
	
	/**
	 * This method paints the specified pixel using the specified blend mode.
	 * 
	 * @param x
	 *            The x coordinate to paint (0 is at the left of the image)
	 * @param y
	 *            The y coordinate to paint (0 is at the bottom of the image)
	 * @param color
	 *            The color to paint with
	 */
	private void paintPixel(int x, int y) {
		if (color.a <= 0)
			return;
		if (x < 0 || y < 0 || x >= width || y >= height)
			return;
		imageRaster.setPixel(x, y, color);
	}

	private int round(double v) {
		return (int) (Math.round(v) & 0xFFFFFFFF);
	}

	private void drawEllipse(int startX, int startY, int width, int height) {
		int xc = (startX + width) / 2;
		int yc = (startY + height) / 2;
		int rx = width / 2;
		int ry = height / 2;
		
		int x, y, p1, p2;
		int rx2 = rx * rx, ry2 = ry * ry;
		x = 0;
		y = ry;

		/* region 1 */
		ellipsePlotPoints(xc, yc, x, y);
		p1 = round(rx2 - (rx2 * ry) + (0.25 * rx2));
		while (ry2 * x <= rx2 * y) {
			if (p1 <= 0) {
				p1 += ry2 * (2 * x + 3);
				x++;
			} else {
				p1 += ry2 * (2 * x + 3) + rx2 * (2 - 2 * y);
				x++;
				y--;
			}
			ellipsePlotPoints(xc, yc, x, y);
		}

		/* region 2 */
		p2 = round(ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1) * (y - 1) - rx2 * ry2);
		while (y >= 0) {
			if (p2 <= 0) {
				p2 += ry2 * (2 * x + 2) + rx2 * (3 - 2 * y);
				x++;
				y--;
			} else {
				p2 += rx2 * (3 - 2 * y);
				y--;
			}
			ellipsePlotPoints(xc, yc, x, y);
		}
	}
	
	private void fillEllipse(int startX, int startY, int width, int height) {
		int xc = (startX + width) / 2;
		int yc = (startY + height) / 2;
		int rx = width / 2;
		int ry = height / 2;
		
		int x, y, p1, p2;
		int rx2 = rx * rx, ry2 = ry * ry;
		x = 0;
		y = ry;

		/* region 1 */
		ellipsePlotLine(xc, yc, x, y);
		p1 = round(rx2 - (rx2 * ry) + (0.25 * rx2));
		while (ry2 * x <= rx2 * y) {
			if (p1 <= 0) {
				p1 += ry2 * (2 * x + 3);
				x++;
			} else {
				p1 += ry2 * (2 * x + 3) + rx2 * (2 - 2 * y);
				x++;
				y--;
			}
			ellipsePlotLine(xc, yc, x, y);
		}

		/* region 2 */
		p2 = round(ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1) * (y - 1) - rx2 * ry2);
		while (y >= 0) {
			if (p2 <= 0) {
				p2 += ry2 * (2 * x + 2) + rx2 * (3 - 2 * y);
				x++;
				y--;
			} else {
				p2 += rx2 * (3 - 2 * y);
				y--;
			}
			ellipsePlotLine(xc, yc, x, y);
		}
	}
	
	private void ellipsePlotLine(int xc, int yc, int x, int y) {
		for (int i = xc - x; i <= xc + x; i++) {
			paintPixel(i, yc + y);
			paintPixel(i, yc - y);
		}
	}
	
	private void ellipsePlotPoints(int xc, int yc, int x, int y) {
		paintPixel(xc + x, yc + y);
		paintPixel(xc - x, yc + y);
		paintPixel(xc + x, yc - y);
		paintPixel(xc - x, yc - y);
	}
	
	private void paintRect() {
		paintRect(0, 0, width-1, height-1);
	}
	
	private void paintEllipse() {
		float alpha = color.a;
		color.a *= 0.25f;
		fillEllipse(1, 0, width-1, height-2);
		color.a = alpha;
		drawEllipse(1, 0, width-1, height-2);
	}
	
	private void paintPolyline() {
		List<Vector2f> points = obj.getPoints();
		paintPolyline(points);
	}
	
	private void paintPolyline(List<Vector2f> points) {
		if (points.size() < 2) {
			throw new IllegalArgumentException("An polygon must have 2 points at least.");
		}
		
		int len = points.size();
		Vector2f start = new Vector2f(points.get(0));
		for(int i=1; i<len; i++) {
			paintLine(start, points.get(i));
			start.set(points.get(i));
		}
	}
	
	private void paintLine(Vector2f start, Vector2f end) {
		int startX = (int) start.x;
		int startY = (int) (height - start.y);
		int endX = (int) end.x;
		int endY = (int) (height - end.y);
		
		paintLine(startX, startY, endX, endY);
	}
}
