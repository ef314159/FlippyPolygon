package shivanhunter.flippypolygon.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import shivanhunter.flippypolygon.FlippyPolygon;

public class HtmlLauncher extends GwtApplication {
        @Override public GwtApplicationConfiguration getConfig () {
        	GwtApplicationConfiguration config = new GwtApplicationConfiguration(512, 512);
        	config.antialiasing = true;
            return config;
        }

        @Override public ApplicationListener getApplicationListener () {
            return new FlippyPolygon();
        }
}