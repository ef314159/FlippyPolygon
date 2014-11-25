package shivanhunter.flippypolygon;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class MainMenu implements Screen, InputProcessor, TweenCallback {
	private ShapeRenderer renderer;
	private TweenManager manager;
	
	private final float MENUITEM_RADIUS;
	private final Polygon tri, quad, pent, hex;
	private int polySelected = -1;
	
	private Game g;
	
	public MainMenu(Game g) {
		this.g = g;
		
		MENUITEM_RADIUS = Gdx.graphics.getWidth()/5;
		
		tri =  new Polygon(3, new Vector2(Gdx.graphics.getWidth()*1/8, Gdx.graphics.getHeight()/2));
		quad = new Polygon(4, new Vector2(Gdx.graphics.getWidth()*3/8, Gdx.graphics.getHeight()/2));
		pent = new Polygon(5, new Vector2(Gdx.graphics.getWidth()*5/8, Gdx.graphics.getHeight()/2));
		hex =  new Polygon(6, new Vector2(Gdx.graphics.getWidth()*7/8, Gdx.graphics.getHeight()/2));
	}
	
	@Override public void show() {
		renderer = new ShapeRenderer();
		manager = new TweenManager();
		
		Gdx.input.setInputProcessor(this);
	}

	@Override public void render(float delta) {
		Gdx.gl.glClearColor(.9f, .9f, .9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		manager.update(delta);
		
		tri.draw(renderer, true);
		quad.draw(renderer, true);
		pent.draw(renderer, true);
		hex.draw(renderer, true);
	}

	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		screenY = Gdx.graphics.getHeight() - screenY;
		
		if (collide(screenX, screenY, tri)) {
			polySelected = 3;
		} else if (collide(screenX, screenY, quad)) {
			polySelected = 4;
		} else if (collide(screenX, screenY, pent)) {
			polySelected = 5;
		} else if (collide(screenX, screenY, hex)) {
			polySelected = 6;
		} else {
			polySelected = -1;
		}
		
		if (polySelected > 0) {
			Gdx.input.setInputProcessor(null);
			
			if (polySelected == 3) tri.dissapear(this, manager);
			else if (polySelected == 4) quad.dissapear(this, manager);
			else if (polySelected == 5) pent.dissapear(this, manager);
			else if (polySelected == 6) hex.dissapear(this, manager);
		}
		
		return true;
	}
	
	private boolean collide(int x, int y, Polygon p) {
		if (p.getCenterPoint().dst(x, y) < MENUITEM_RADIUS) return true;
		else return false;
	}
	
	/**
	 * A Tween callback event: called when the polygon finishes disappearing
	 * after game end.
	 */
	@Override public void onEvent(int arg0, BaseTween<?> arg1) {
		g.setScreen(new GameScreen(g, polySelected, 3, 0, 1));
		dispose();
	}
	
	@Override public void dispose() {
	}

	// unused Screen methods
	@Override public void resize(int width, int height) { }
	@Override public void hide() { }
	@Override public void pause() { }
	@Override public void resume() { }

	// unused InputProcessor methods
	@Override public boolean keyDown(int keycode) {return false;}
	@Override public boolean keyUp(int keycode) {return false;}
	@Override public boolean keyTyped(char character) {return false;}
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {return false;}
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
	@Override public boolean mouseMoved(int screenX, int screenY) {return false;}
	@Override public boolean scrolled(int amount) {return false;}

}
