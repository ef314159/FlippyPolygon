package shivanhunter.flippypolygon.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import shivanhunter.flippypolygon.FlippyPolygon;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 512;
		config.width = 512;
		new LwjglApplication(new FlippyPolygon(), config);
	}
}
