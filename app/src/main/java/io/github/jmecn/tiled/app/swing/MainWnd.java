package io.github.jmecn.tiled.app.swing;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.system.awt.AwtPanel;
import io.github.jmecn.tiled.app.jme3.MyFileLocator;
import io.github.jmecn.tiled.app.jme3.TiledApp;
import io.github.jmecn.tiled.core.TiledMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class MainWnd extends JFrame {

    public static final String APP_NAME = "Tiled Map Viewer";
    public static final String CONFIG_FILE = ".config";
    public static final String LAST_DIR = "lastDir";
    public static final String RECENT_FILES = "recentFiles";
    public static final int RECENT_FILE_MAX = 50;

    static Logger log = LoggerFactory.getLogger(MainWnd.class.getName());

    private final transient TiledApp app;

    private Properties properties;
    private File config;

    private final LinkedList<RecentFile> recentFiles = new LinkedList<>();

    private JLabel mapStatus;
    private JLabel cursorStatus;
    private final JFileChooser fileChooser;
    private LayerView layerView;
    private JMenu recentFilesMenu;

    public MainWnd(TiledApp app, AwtPanel awtPanel) {
        super(APP_NAME);

        // load properties
        readProperties();
        // load recent files
        readRecentFiles();

        this.app = app;

        setSize(800, 600);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                app.stop();
            }
        });

        this.fileChooser = createFileChooser();
        this.layerView = createLayerView();
        this.setJMenuBar(createMenuBar());

        JPanel panel = createContentPanel();
        panel.add(awtPanel, BorderLayout.CENTER);
        this.setContentPane(panel);

        this.pack();

        /*
         * center
         */
        center();
    }

    private void readProperties() {
        config = new File(CONFIG_FILE);
        properties = new Properties();
        if (config.exists()) {
            try (FileInputStream in = new FileInputStream(config)) {
                properties.load(new BufferedInputStream(in, 4096));
            } catch (Exception e) {
                log.error("Failed to load properties.", e);
            }
        }
    }

    private void writeProperties() {
        try (FileOutputStream out = new FileOutputStream(config, false)) {
            properties.store(new BufferedOutputStream(out, 4096), "Tiled Map Viewer Properties");
        } catch (Exception e) {
            log.error("Failed to save properties.", e);
        }
    }

    private void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open");
        openItem.setMnemonic('O');
        openItem.addActionListener(e -> load());
        fileMenu.add(openItem);

        recentFilesMenu = new JMenu("Recent Files");
        updateRecentFileMenu();
        fileMenu.add(recentFilesMenu);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(e -> {
            app.stop();
            dispose();
        });
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        menuBar.add(viewMenu);

        JCheckBoxMenuItem layerItem = new JCheckBoxMenuItem("Layers");
        layerItem.setMnemonic('L');
        layerItem.addActionListener(e -> showLayers());
        viewMenu.add(layerItem);

        JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Show Grid");
        gridItem.setMnemonic('G');
        gridItem.addActionListener(e -> app.toggleGrid());
        viewMenu.add(gridItem);

        return menuBar;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        panel.add(tabbedPane, BorderLayout.WEST);
        tabbedPane.addTab("Layers", new JScrollPane(layerView));

        panel.add(createStatusBar(), BorderLayout.PAGE_END);
        return panel;
    }

    private LayerView createLayerView() {
        layerView = new LayerView();
        return layerView;
    }

    private JPanel createStatusBar() {
        this.mapStatus = new JLabel("Map: None");
        this.cursorStatus = new JLabel("Ready.");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(mapStatus);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(cursorStatus);

        return panel;
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        // restore last directory
        String dir = properties.getProperty(LAST_DIR, ".");
        File lastDir = new File(dir);
        if (lastDir.isDirectory() && lastDir.exists()) {
            chooser.setCurrentDirectory(lastDir);
        }

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().endsWith(".tmx");
            }

            @Override
            public String getDescription() {
                return "Tiled Map Files (*.tmx)";
            }
        });

        return chooser;
    }

    public void setMapStatus(String status) {
        mapStatus.setText(status);
    }

    public void setCursorStatus(String status) {
        cursorStatus.setText(status);
    }

    private void showLayers() {
        // TODO
    }

    private void load(RecentFile file) {

        AssetManager assetManager = app.getAssetManager();
        log.info("Load:{}", file.getAbsolutePath());
        TiledMap map = null;
        try {
            assetManager.registerLocator(file.getFolder(), MyFileLocator.class);
            map = (TiledMap) assetManager.loadAsset(file.getName());
            // remove from cache, in case of reload
            assetManager.deleteFromCache(new AssetKey<>(file.getName()));
        } catch (Exception e) {
            log.error("Failed to load {}", file.getAbsolutePath(), e);
        } finally {
            assetManager.unregisterLocator(file.getFolder(), MyFileLocator.class);
        }

        if (map != null) {
            layerView.setTiledMap(map);
            app.load(map);
            // update the window title
            this.setTitle(APP_NAME + " - " + file.getName());
        }
    }

    private void load() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();

            // save last directory
            properties.setProperty(LAST_DIR, file.getParent());
            writeProperties();
            fileChooser.setCurrentDirectory(file.getParentFile());

            AssetManager assetManager = app.getAssetManager();
            log.info("Load:{}", file.getAbsoluteFile());
            TiledMap map = null;
            try {
                assetManager.registerLocator(file.getParent(), MyFileLocator.class);
                map = (TiledMap) assetManager.loadAsset(fileName);
                // remove from cache, in case of reload
                assetManager.deleteFromCache(new AssetKey<>(fileName));
            } catch (Exception e) {
                log.error("Failed to load {}", file.getAbsoluteFile(), e);
            } finally {
                assetManager.unregisterLocator(file.getParent(), MyFileLocator.class);
            }

            if (map != null) {
                layerView.setTiledMap(map);
                app.load(map);
                // update the window title
                this.setTitle(APP_NAME + " - " + fileName);
                this.saveRecentFile(file);
            }
        }
    }

    private void saveRecentFile(File file) {
        String path = file.getAbsolutePath();
        for (RecentFile recentFile : recentFiles) {
            if (recentFile.getAbsolutePath().equals(path)) {
                recentFiles.remove(recentFile);
                break;
            }
        }

        recentFiles.addFirst(new RecentFile(path));
        if (recentFiles.size() > RECENT_FILE_MAX) {
            recentFiles.removeLast();
        }

        StringBuilder sb = new StringBuilder();
        for (RecentFile recentFile : recentFiles) {
            sb.append(recentFile.getAbsolutePath()).append(";");
        }

        properties.setProperty(RECENT_FILES, sb.toString());
        writeProperties();

        updateRecentFileMenu();
    }

    private void readRecentFiles() {
        String files = properties.getProperty(RECENT_FILES);
        if (files != null) {
            String[] paths = files.split(";");
            for (String path : paths) {
                if (new File(path).exists()) {
                    recentFiles.add(new RecentFile(path));
                }
            }
        }
    }

    private void updateRecentFileMenu() {
        recentFilesMenu.removeAll();
        for (RecentFile recentFile : recentFiles) {
            JMenuItem item = new JMenuItem(recentFile.getName());
            item.addActionListener(e1 -> {
                File file = new File(recentFile.getAbsolutePath());
                if (file.exists()) {
                    load(recentFile);
                } else {
                    recentFiles.remove(recentFile);
                    saveRecentFile(file);
                }
            });
            recentFilesMenu.add(item);
        }

        if (recentFiles.isEmpty()) {
            JMenuItem item = new JMenuItem("No recent files");
            item.setEnabled(false);
            recentFilesMenu.add(item);
        } else {
            recentFilesMenu.addSeparator();
            JMenuItem item = getClearRecentFilesItem();
            recentFilesMenu.add(item);
        }
    }

    private JMenuItem getClearRecentFilesItem() {
        JMenuItem item = new JMenuItem("Clear");
        item.addActionListener(e -> {
            // Yes or No
            int ret = JOptionPane.showConfirmDialog(this, "Are you sure to clear all recent files?", "Clear Recent Files",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (ret != JOptionPane.YES_OPTION) {
                return;
            }
            recentFiles.clear();
            properties.remove(RECENT_FILES);
            writeProperties();
            updateRecentFileMenu();
        });
        return item;
    }
}
