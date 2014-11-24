package shivanhunter.flippypolygon;

import java.util.Random;

import com.badlogic.gdx.Game;

public class FlippyPolygon extends Game {
	Random RNG;
	
	@Override public void create () {
		setScreen(new MainMenu(this));
	}
}
