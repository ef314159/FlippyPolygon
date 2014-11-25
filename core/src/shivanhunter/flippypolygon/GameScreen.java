package shivanhunter.flippypolygon;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GameScreen implements Screen, InputProcessor, TweenCallback {
	private Polygon poly;
	private Polygon destination;
	private ShapeRenderer renderer;
	private TweenManager manager;

	private SpriteBatch batch;
	private BitmapFont smallText, largeText;
	
	private Game g;
	private int numVertices;
	private int numMoves;
	
	private int movesMade = 0;
	private int level;
	private float score;
	
	private final int RESET_X;
	private final int RESET_Y;
	private String RESETSTR = "NEW LEVEL";
	
	private static final float CHANCE_TO_INCREASE_MOVES = 0.1f;
	
	/**
	 * Creates a new GameScreen. A GameScreen represents a single "level" of
	 * the game; once the level is finished, this is replaced by a new
	 * GameScreen. A GameScreen contains a single player-controlled polygon
	 * and destination.
	 * 
	 * Two factors control difficulty: the number of vertices the polygon has,
	 * and the number of random moves made initially to move it away from the
	 * destination.
	 * 
	 * @param g the Game using this Screen.
	 * @param numVertices the number of vertices the polygon has
	 * @param numMoves the number of initial moves to make
	 */
	public GameScreen(Game g, int numVertices, int numMoves, float score, int level) {
		this.g = g;
		this.numVertices = numVertices;
		this.numMoves = numMoves;
		
		this.score = score;
		this.level = level;
		
		// place polygon in the center of the screen
		Vector2 centerPoint = new Vector2(
				Gdx.graphics.getWidth()/2,
				Gdx.graphics.getHeight()/2);
		
		poly = new Polygon(numVertices, centerPoint);
		
		// copy destination before moving p
		destination = new Polygon(poly);

		// move towards a random location
		int x = MathUtils.random(Gdx.graphics.getWidth());
		int y = MathUtils.random(Gdx.graphics.getHeight());
		
		// for n moves
		for (int i = 0; i < numMoves; ++i) {
			// to reduce moves being made and immediately unmade,
			// only move the location occasionally
			if (MathUtils.randomBoolean(.25f))
				x = MathUtils.random(Gdx.graphics.getWidth());
			if (MathUtils.randomBoolean(.25f))
				y = MathUtils.random(Gdx.graphics.getHeight());
			
			poly.flipTowards(x, y);
		}
		
		RESET_X = Gdx.graphics.getWidth()/2;
		RESET_Y = 32;
	}

	/**
	 * Handles libGDX setup stuff - renderers and input processors. Called when
	 * the Screen gains focus in a Game.
	 */
	@Override public void show() {
		renderer = new ShapeRenderer();
		manager = new TweenManager();
		batch = new SpriteBatch();
		
		smallText = new BitmapFont();
		smallText.setColor(1, .1f, .1f, 1);
		smallText.setUseIntegerPositions(false);
		smallText.setScale(1.5f);
		
		largeText = new BitmapFont();
		largeText.setColor(1, .1f, .1f, 1);
		largeText.setUseIntegerPositions(false);
		largeText.setScale(2);

		Gdx.input.setInputProcessor(this);
		Tween.registerAccessor(Polygon.class, new Polygon());
	}

	/**
	 * Called continuously to update the screen and game state.
	 */
	@Override public void render(float delta) {
		Gdx.gl.glClearColor(.9f, .9f, .9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		manager.update(delta);
		poly.draw(renderer, true);
		destination.draw(renderer, false);
		
		if (poly.equals(destination)) {
			endScreen();
		}
		
		batch.begin();
		
		String levelStr = "Level: " + level;
		String scoreStr = "Score: " + (score + (float)numMoves/Math.max(numMoves, movesMade));
		
		smallText.draw(batch, levelStr,
				10,
				Gdx.graphics.getHeight() - 10);
		
		largeText.draw(batch, scoreStr,
				Gdx.graphics.getWidth() - largeText.getBounds(scoreStr).width - 10,
				Gdx.graphics.getHeight() - 10);
		
		largeText.draw(batch, RESETSTR,
				RESET_X - largeText.getBounds(RESETSTR).width/2,
				RESET_Y);
		
		batch.end();
	}

	/**
	 * LibGDX input method: handles the case where a screen touch is released
	 */
	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		screenY = Gdx.graphics.getHeight() - screenY;
		
		if (    screenX > RESET_X - largeText.getBounds(RESETSTR).width/2 &&
				screenX < RESET_X + largeText.getBounds(RESETSTR).width/2 &&
				screenY > RESET_Y - largeText.getBounds(RESETSTR).height &&
				screenY < RESET_Y) {
			Gdx.input.setInputProcessor(null);
			
			movesMade = Integer.MAX_VALUE;
			
			endScreen();
		} else if (poly != null) {
			if (poly.flipTowards(screenX, screenY, manager)) {
				movesMade++;
			}
		}
		return true;
	}
	
	/**
	 * Ends this screen and moves to the next one.
	 */
	private void endScreen() {
		poly.pause();
		poly.dissapear(this, manager);
	}
	
	/**
	 * A Tween callback event: called when the polygon finishes disappearing
	 * after game end.
	 */
	@Override public void onEvent(int arg0, BaseTween<?> arg1) {
		// possibly increase the number of moves, increasing diffculty
		int newNumMoves = numMoves;
		if (MathUtils.randomBoolean(CHANCE_TO_INCREASE_MOVES)) newNumMoves++;
		
		// switch to new Screen
		g.setScreen(new GameScreen(
				g,
				numVertices,
				newNumMoves,
				score + (float)numMoves/Math.max(numMoves, movesMade),
				level + 1));
		dispose();
	}
	
	@Override public void dispose() {
		//batch.dispose();
		//renderer.dispose();
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
