package shivanhunter.flippypolygon;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Polygon implements TweenAccessor<Polygon>, TweenCallback {
	private float[] verts;
	private Vector2 centerPoint;
	private boolean paused = false;
	
	private static final float DISAPPEAR_TIME = 0.25f;
	private static final float FLIP_TIME = 0.15f;
	
	public Polygon() { }
	
	public Polygon(Polygon other) {
		// clone() on float[] apparently doesn't work in gwt lol
		verts = new float[other.verts.length];
		for (int i = 0; i < verts.length; ++i) {
			verts[i] = other.verts[i];
		}
		
		findCenterPoint();
	}
	
	public Polygon (int numVertices, Vector2 centerPoint) {
		// set up vert array and angle var for setting vertex locations
		verts = new float[numVertices*2];
		
		// start at a random angle
		float angle = MathUtils.random(MathUtils.PI2);
		
		// create vert locations - each is between 0.5 and 1.5 units from (0, 0)
		// shoot out distances from the center to each vertex
		for (int i = 0; i < verts.length; i += 2) {
			
			// set a minimum distance for each shoot to ensure a decent size
			// these values for the minimum also ensure a convex polygon
			// heptagons and above are forced to regular polygons using this scheme
			float min = MathUtils.clamp((numVertices - 3.5f)/4f, .25f, 1);
			
			// distance is a random float from min to 1
			float distance = MathUtils.random(min, 1);
			
			// scale up the shoot to a viewable size
			distance *= 50;
			
			// place vertex
			verts[i] = distance*MathUtils.cos(angle) + centerPoint.x;
			verts[i+1] = distance*MathUtils.sin(angle) + centerPoint.y;
			
			// increment angle
			angle += MathUtils.PI2/numVertices;
		}
		
		// create center point
		findCenterPoint();
	}
	
	public Vector2 getCenterPoint() {
		return centerPoint;
	}
	
	public static float EQUALS_TOLERANCE = 2;
	
	@Override public boolean equals(Object other) {
		if (other == null || !(other instanceof Polygon)) return false;
		
		Polygon p = (Polygon) other;
		
		if (p.verts.length != verts.length) return false;
		
		for (int i = 0; i < verts.length; i++) {
			if (Math.abs(verts[i] - p.verts[i]) > EQUALS_TOLERANCE) return false;
		}
		
		return true;
	}
	
	public void pause() {
		paused = true;
	}
	
	public void unpause() {
		paused = false;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Draws this polygon given a ShapeRenderer context. Can be drawn filled or
	 * as an outline.
	 * 
	 * @param renderer the renderer to draw on
	 * @param fill whether the polygon should be filled
	 */
	public void draw(ShapeRenderer renderer, boolean fill) {
		if (fill) {
			renderer.setColor(.2f, .2f, .2f, 1);
			renderer.begin(ShapeType.Filled);
			
			for (int i = 2; i < verts.length - 2; i += 2) {
				renderer.triangle(verts[0], verts[1],
						verts[i], verts[i+1], verts[i+2], verts[i+3]);
			}
		} else {
			renderer.setColor(.5f, .5f, .5f, 1);
			renderer.begin(ShapeType.Line);
			
			renderer.polygon(verts);
		}
		renderer.end();
	}
	
	/**
	 * Flips the polygon on a certain edge to move it closer to the target point.
	 * Accepts a tween manager to display a smooth transition between states.
	 * Calling this function does not guarantee that the polygon will flip: it
	 * is possible for a polygon to be "paused" when it is already tweening from
	 * one state to another. The return value insicates if the flip occurred.
	 * 
	 * @param targetX the x coordinate of the target
	 * @param targetY the y coordinate of the target
	 * @param manager the manager to handle the tween
	 * @return true if a flip occurred
	 */
	public boolean flipTowards(float targetX, float targetY, TweenManager manager) {
		if (!paused) {
			/* pause the polygon so it can't start a new tween while moving
			 * (the new tween would use mid-interpolation values, resulting in
			 * a smaller, distorted polygon)
			 */
			paused = true;
			
			// get flipped verts to tween to
			float[] flippedVerts = new float[verts.length];
			flipTowards(targetX, targetY, flippedVerts);
			
			// tween each vert except the last to the flipped verts
			for (int i = 0; i < verts.length - 2; i += 2) {
				int vertIndex = i/2;
				Tween
					.to(this, vertIndex, FLIP_TIME)
					.target(flippedVerts[i], flippedVerts[i+1])
					.start(manager);
			}
			
			// tween the last vert with a callback to reverse vertex order afterwards
			// this callback must be done only once, hence separating the last vertex
			int i = verts.length-2;
			int vertIndex = i/2;
			
			Tween.to(this, vertIndex, FLIP_TIME)
				.target(flippedVerts[i], flippedVerts[i+1])
				.setCallback(this)
				.start(manager);
			
			// the flip was accomplished
			return true;
		} else {
			// no flip was done; polygon is already paused
			return false;
		}
	}
	
	/**
	 * Flips the polygon on a certain edge to move it closer to the target point.
	 * This function executes instantly to change the polygon's position.
	 * Calling this function does not guarantee that the polygon will flip: it
	 * is possible for a polygon to be "paused" when it is already tweening from
	 * one state to another. The return value insicates if the flip occurred.
	 * 
	 * @param targetX the x coordinate of the target
	 * @param targetY the y coordinate of the target
	 * @return true if a flip occurred
	 */
	public boolean flipTowards(float targetX, float targetY) {
		if (!paused) {
			flipTowards(targetX, targetY, verts);
			findCenterPoint();
			reverseVerts();
			return true;
		} else return false;
	}
	
	/**
	 * Uses a tween to shrink this polygon to a point. The tween calls the
	 * given callback when finished.
	 * 
	 * @param callback the callback to execute when finished
	 * @param manager the manager to handle the tween
	 */
	public void dissapear(TweenCallback callback, TweenManager manager) {
		for (int i = 0; i < verts.length; i += 2) {
			int vertIndex = i/2;
			Tween
				.to(this, vertIndex, DISAPPEAR_TIME)
				.target(centerPoint.x, centerPoint.y)
				.setCallback(callback)
				.start(manager);
		}
	}
	
	private void findCenterPoint() {
		// set center to (0, 0), create it if null
		if (centerPoint == null) centerPoint = new Vector2(0, 0);
		else {
			centerPoint.x = 0;
			centerPoint.y = 0;
		}
		
		// get center of polygon by averaging vertex positions
		for (int i = 0; i < verts.length; i += 2) {
			centerPoint.x += verts[i];
			centerPoint.y += verts[i+1];
		}
		centerPoint.x /= verts.length/2;
		centerPoint.y /= verts.length/2;
	}
	
	private void flipTowards(float targetX, float targetY, float[] flippedVerts) {
		// first find the edge to flip across
		int index2 = getSelectedEdge(targetX, targetY);
		int index1 = index2 - 2;
		
		// sanitize index1 if it's below 0 (if index2 was 0th edge)
		if (index1 < 0) index1 += verts.length;
		
		//verts[index1] += 1;
		//verts[index2] += 1;
		
		// the reflection line must pass through the origin
		// use v1 as the offset to subtract from each reflected point
		Vector2 offset = new Vector2(
				verts[index1],
				verts[index1+1]);
		
		// the vector representing the reflection line is vert2 - vert1
		Vector2 reflection = new Vector2(
				verts[index2] - verts[index1],
				verts[index2+1] - verts[index1+1]);
		
		// reflect each point individually
		Vector2 point = new Vector2(), projection = new Vector2();
		for (int i = 0; i < verts.length; i += 2) {
			point.set(verts[i], verts[i+1]);
			point.sub(offset);
			
			// find the projection of the point onto the reflection line
			projection.set(reflection);
			projection.scl(point.dot(reflection) / reflection.dot(reflection));
			
			// multiply by 2 and subtract point to get the reflected point
			point.set(projection.scl(2).sub(point));
			point.add(offset);
			
			flippedVerts[i] = point.x;
			flippedVerts[i+1] = point.y;
		}
	}
	
	/**
	 * Given a target point in the plane, finds an edge facing towards that point.
	 * 
	 * @param targetX the x coordinate of the target point
	 * @param targetY the y coordinate of the target point
	 * @return the index of the second point in the edge - the other index will be this minus 2
	 */
	private int getSelectedEdge(float targetX, float targetY) {
		// get angle of line from center to target
		float targetAngle = MathUtils.atan2(
				targetY - centerPoint.y,
				targetX - centerPoint.x);
		
		// get angle of line from center to last vert
		float vertAngle2, vertAngle1 = MathUtils.atan2(
				verts[verts.length-1] - centerPoint.y,
				verts[verts.length-2] - centerPoint.x);
		
		// iterate through all vertices
		for (int i = 0; i < verts.length; i += 2) {
			// get angle from center to next vert, starting at 0
			vertAngle2 = MathUtils.atan2(
					verts[i+1] - centerPoint.y,
					verts[i] - centerPoint.x);

			// when the line to center is between the lines to verts 1 and 2, use the edge
			// formed by verts 1 and 2
			if ((vertAngle1 <= targetAngle && targetAngle <= vertAngle2) ||
					vertAngle1 > vertAngle2 &&
					(vertAngle2 >= targetAngle || vertAngle1 <= targetAngle)) {
				// if found, return i (representing the second vertex in the edge)
				return i;
			}
			
			// increment index if not found
			vertAngle1 = vertAngle2;
		}
		
		// error, not found
		return -1;
	}
	
	/**
	 * Called by a callback when verts are in reverse order because of flipping.
	 * Must correct the vert order so that getSelectedEdge() will work
	 */
	private void reverseVerts() {
		// iterate through verts from front and back, swapping each vert
		for (int i = 0, j = verts.length-2; i < j; i += 2, j-=2) {
			float temp = verts[i];
			verts[i] = verts[j];
			verts[j] = temp;

			temp = verts[i+1];
			verts[i+1] = verts[j+1];
			verts[j+1] = temp;
		}
	}
	
	public void printVerts() {
		System.out.println("");
		for (int i = 0; i < verts.length; i += 2) {
			System.out.println("(" + verts[i] + ", " + verts[i+1] + ")");
		}
	}

	// tween engine methods
	@Override public int getValues(Polygon p, int index, float[] coords) {
		coords[0] = p.verts[index*2];
		coords[1] = p.verts[index*2 + 1];
		return 2;
	}

	@Override public void setValues(Polygon p, int index, float[] coords) {
		p.verts[index*2] = coords[0];
		p.verts[index*2 + 1] = coords[1];
	}

	/* 
	 * Called by the tween engine as a callback once tweening has finished.
	 * Frees the polygon up for further flippage.
	 */
	@Override public void onEvent(int arg0, BaseTween<?> arg1) {
		paused = false;
		reverseVerts();
		findCenterPoint();
	}
}
