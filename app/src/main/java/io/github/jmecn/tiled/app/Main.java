package io.github.jmecn.tiled.app;

import com.jme3.system.AppSettings;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

/**
 * This is the main class of the application.
 *
 * @author yanmaoyuan
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // ignored
        }

        CountDownLatch latch = new CountDownLatch(1);

        AppSettings settings = new AppSettings(true);
        settings.setCustomRenderer(AwtPanelsContext.class);
        settings.setGammaCorrection(false);
        settings.setFrameRate(60);

        TiledApp app = new TiledApp(latch);
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();

        SwingUtilities.invokeLater(() -> {
            final AwtPanelsContext ctx = (AwtPanelsContext) app.getContext();
            AwtPanel panel = ctx.createPanel(PaintMode.Accelerated);
            panel.setPreferredSize(new Dimension(800, 600));
            ctx.setInputSource(panel);

            app.setPanel(panel);

            // init window
            MainWnd wnd = new MainWnd(app, panel);
            wnd.setVisible(true);
            app.setWnd(wnd);

            // Both panels are ready.
            latch.countDown();
        });
    }
}
