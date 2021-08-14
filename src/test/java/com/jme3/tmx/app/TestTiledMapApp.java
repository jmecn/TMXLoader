package com.jme3.tmx.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;
import com.jme3.tmx.TiledMapAppState;
import com.jme3.tmx.TmxLoader;
import com.jme3.tmx.core.TiledMap;

/**
 * Chooser a tmx file and load it.
 * 
 * @author yanmaoyuan
 * 
 */
public class TestTiledMapApp extends SimpleApplication {

	final static private String[] assets = {
			"Models/Examples/BeatBoss/forest.tmx",
			"Models/Examples/BeatBoss/cave.tmx",
			"Models/Examples/BeatBoss/tomb.tmx",
			
			"Models/Examples/Orthogonal/01.tmx",
			"Models/Examples/Orthogonal/02.tmx",
			"Models/Examples/Orthogonal/03.tmx",
			"Models/Examples/Orthogonal/04.tmx",
			"Models/Examples/Orthogonal/05.tmx",
			"Models/Examples/Orthogonal/06.tmx",
			"Models/Examples/Orthogonal/07.tmx",
			"Models/Examples/Orthogonal/orthogonal-outside.tmx",
			"Models/Examples/Orthogonal/perspective_walls.tmx",
			"Models/Examples/csvmap.tmx", "Models/Examples/sewers.tmx",
			"Models/Examples/Desert/desert.tmx",

			"Models/Examples/Isometric/01.tmx",
			"Models/Examples/Isometric/02.tmx",
			"Models/Examples/Isometric/03.tmx",
			"Models/Examples/Isometric/isometric_grass_and_water.tmx",

			"Models/Examples/Hexagonal/01.tmx",
			"Models/Examples/Hexagonal/02.tmx",
			"Models/Examples/Hexagonal/03.tmx",
			"Models/Examples/Hexagonal/04.tmx",
			"Models/Examples/Hexagonal/05.tmx",
			"Models/Examples/Hexagonal/hexagonal-mini.tmx",

			"Models/Examples/Staggered/01.tmx",
			"Models/Examples/Staggered/02.tmx",
			"Models/Examples/Staggered/03.tmx",
			"Models/Examples/Staggered/04.tmx",
			"Models/Examples/Staggered/05.tmx", };

	final static private String[] names = { 
			"forest", "cave", "tomb",
			"orthogonal_01", "orthogonal_02",
			"orthogonal_03", "orthogonal_04", "orthogonal_05", "orthogonal_06",
			"orthogonal_07", "orthogonal_outside", "orthogonal_perspective_walls", "orthogonal_csvmap",
			"orthogonal_sewers", "orthogonal_desert",

			"isometric_01", "isometric_02", "isometric_03", "isometric_grass_and_water",

			"hexagonal_01", "hexagonal_02", "hexagonal_03", "hexagonal_04",
			"hexagonal_05", "hexagonal_mini",

			"staggered_01", "staggered_02", "staggered_03", "staggered_04",
			"staggered_05", };

	private static String assetPath = null;

	final private static CountDownLatch panelsAreReady = new CountDownLatch(1);
	private static TestTiledMapApp app;
	private static AwtPanel panel;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		app = new TestTiledMapApp();
		app.setShowSettings(false);
		AppSettings settings = new AppSettings(true);
		settings.setCustomRenderer(AwtPanelsContext.class);
		settings.setSamples(4);
		settings.setGammaCorrection(false);
		settings.setFrameRate(60);
		app.setSettings(settings);
		app.start();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				/*
				 * Sleep 2 seconds to ensure there's no race condition. The
				 * sleep is not required for correctness.
				 */
				try {
					Thread.sleep(2000);
				} catch (InterruptedException exception) {
					return;
				}

				final AwtPanelsContext ctx = (AwtPanelsContext) app
						.getContext();
				panel = ctx.createPanel(PaintMode.Accelerated);
				panel.setPreferredSize(new Dimension(800, 600));
				ctx.setInputSource(panel);

				/*
				 * create JFrame
				 */
				final JFrame frame = new JFrame("Test Tiled Map Loader");
				frame.getContentPane().setLayout(new BorderLayout());
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.getContentPane().add(panel, BorderLayout.CENTER);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						app.stop();
					}
				});

				/*
				 * create JList
				 */
				final JList<String> list = new JList<String>();
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				DefaultListModel<String> model = new DefaultListModel<String>();
				for (int i = 0; i < names.length; i++) {
					model.addElement(names[i]);
				}

				list.setModel(model);

				frame.getContentPane().add(new JScrollPane(list),
						BorderLayout.WEST);

				list.getSelectionModel().addListSelectionListener(
						new ListSelectionListener() {
							public void valueChanged(ListSelectionEvent e) {
								assetPath = assets[list.getSelectedIndex()];
							}
						});
				list.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && assetPath != null) {
							app.load(assetPath);
						}
					}
				});
				list.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							app.load(assetPath);
						} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
							frame.dispose();
						}
					}
				});

				final JPanel buttonPanel = new JPanel(new FlowLayout(
						FlowLayout.CENTER));
				frame.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

				final JButton okButton = new JButton("Ok");
				okButton.setMnemonic('O');
				buttonPanel.add(okButton);
				frame.getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						app.load(assetPath);
					}
				});

				final JButton cancelButton = new JButton("Cancel");
				cancelButton.setMnemonic('C');
				buttonPanel.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.dispose();
					}
				});

				frame.pack();

				/*
				 * center
				 */
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				Dimension frameSize = frame.getSize();
				if (frameSize.height > screenSize.height) {
					frameSize.height = screenSize.height;
				}
				if (frameSize.width > screenSize.width) {
					frameSize.width = screenSize.width;
				}
				frame.setLocation((screenSize.width - frameSize.width) / 2,
						(screenSize.height - frameSize.height) / 2);

				frame.setVisible(true);
				/*
				 * Both panels are ready.
				 */
				panelsAreReady.countDown();
			}
		});
	}

	@Override
	public void simpleInitApp() {
		
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		stateManager.attachAll(new TiledMapAppState());

		/*
		 * Wait until both AWT panels are ready.
		 */
		try {
			panelsAreReady.await();
		} catch (InterruptedException exception) {
			throw new RuntimeException("Interrupted while waiting for panels",
					exception);
		}

		panel.attachTo(true, viewPort, guiViewPort);
	}

	public void load(final String assetPath) {
		enqueue(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				TiledMap map = null;
				try {
					map = (TiledMap) assetManager.loadAsset(assetPath);
				} catch (Exception e) {
					// i don't care
				}

				if (map != null) {
					TiledMapAppState tiledMap = stateManager.getState(TiledMapAppState.class);
					tiledMap.setMap(map);

					// look at the center of this map
					tiledMap.moveToTile(map.getWidth() * 0.5f, map.getHeight() * 0.5f);
				}
				return null;
			}

		});

	}
}