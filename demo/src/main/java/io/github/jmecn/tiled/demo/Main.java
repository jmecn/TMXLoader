package io.github.jmecn.tiled.demo;

import com.jme3.system.AppSettings;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Main {
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Tiled Demo");
        settings.setResolution(900, 640);
        settings.setGammaCorrection(false);
        settings.setFrameRate(60);
        settings.setVSync(true);

        Demo demo = new Demo();
        demo.setSettings(settings);
        demo.start();
    }
}
