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

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;

/**
 * 
 * @author yanmaoyuan
 * 
 */
public class TestJFrame extends SimpleApplication {

	final static private String[] names = { "forest", "cave", "tomb"};

	final private static CountDownLatch panelsAreReady = new CountDownLatch(1);
	private static TestJFrame app;
	private static AwtPanel panel;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		app = new TestJFrame();
		app.setShowSettings(false);
		AppSettings settings = new AppSettings(true);
		settings.setCustomRenderer(AwtPanelsContext.class);
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
				final JFrame frame = new JFrame("Test JFrame");
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

				list.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						String value = list.getSelectedValue();
						app.load(value);
					}
				});
				list.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							String value = list.getSelectedValue();
							app.load(value);
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
						String value = list.getSelectedValue();
						app.load(value);
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
		
		/*
		 * Wait until both AWT panels are ready.
		 */
		try {
			panelsAreReady.await();
		} catch (InterruptedException exception) {
			throw new RuntimeException("Interrupted while waiting for panels", exception);
		}

		panel.attachTo(true, viewPort, guiViewPort);
		
		flyCam.setDragToRotate(true);
	}

	public void load(final String assetPath) {
		enqueue(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				System.out.println(assetPath);
				return null;
			}

		});

	}
}