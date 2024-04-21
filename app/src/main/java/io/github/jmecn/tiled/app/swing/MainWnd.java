package io.github.jmecn.tiled.app.swing;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.system.awt.AwtPanel;
import io.github.jmecn.tiled.app.jme3.MyFileLocator;
import io.github.jmecn.tiled.app.jme3.TiledApp;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TileLayer;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.math2d.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
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
    private transient TiledMap map;
    private transient Layer layer;

    private Properties properties;
    private File config;
    private boolean isGridVisible;
    private boolean isCursorVisible;
    private boolean isParallaxEnabled;

    private final LinkedList<RecentFile> recentFiles = new LinkedList<>();

    private JLabel mapStatus;
    private JLabel cursorStatus;
    private final JFileChooser fileChooser;
    private final LayerView layerView;
    private final PropertyTable propertyTable;
    private JMenu recentFilesMenu;

    public MainWnd(TiledApp app, AwtPanel awtPanel) {
        super(APP_NAME);

        // load properties
        readProperties();
        // load recent files
        readRecentFiles();

        this.isGridVisible = app.isGridVisible();
        this.isCursorVisible = app.isCursorVisible();
        this.isParallaxEnabled = app.isParallaxEnabled();

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
        this.layerView = new LayerView();
        this.propertyTable = new PropertyTable();
        this.setJMenuBar(createMenuBar());

        JPanel panel = createContentPanel();
        panel.add(awtPanel, BorderLayout.CENTER);
        this.setContentPane(panel);

        this.pack();

        layerView.addTreeSelectionListener(e -> {
            layerView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) layerView.getLastSelectedPathComponent();
            if (node != null) {
                layer = (Layer) node.getUserObject();
                propertyTable.setLayer(layer);
            }
        });
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
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        recentFilesMenu = new JMenu("Recent Files");
        updateRecentFileMenu();
        fileMenu.add(recentFilesMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        menuBar.add(viewMenu);

        JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Show Grid");
        gridItem.setMnemonic('G');
        gridItem.setSelected(isGridVisible);
        gridItem.addActionListener(e -> setGridVisible());
        viewMenu.add(gridItem);

        JCheckBoxMenuItem cursorItem = new JCheckBoxMenuItem("Show Cursor");
        cursorItem.setMnemonic('C');
        cursorItem.setSelected(isCursorVisible);
        cursorItem.addActionListener(e -> setCursorVisible());
        viewMenu.add(cursorItem);

        JCheckBoxMenuItem parallaxItem = new JCheckBoxMenuItem("Enable Parallax");
        parallaxItem.setMnemonic('P');
        parallaxItem.setSelected(isParallaxEnabled);
        parallaxItem.addActionListener(e -> setParallaxEnable());
        viewMenu.add(parallaxItem);

        return menuBar;
    }

    private void setGridVisible() {
        isGridVisible = !isGridVisible;
        app.setGridVisible(isGridVisible);
    }

    private void setCursorVisible() {
        isCursorVisible = !isCursorVisible;
        app.setCursorVisible(isCursorVisible);
    }

    private void setParallaxEnable() {
        isParallaxEnabled = !isParallaxEnabled;
        app.setParallaxEnabled(isParallaxEnabled);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane propertyScrollPane = new JScrollPane(propertyTable);
        JScrollPane layerScrollPane = new JScrollPane(layerView);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setTopComponent(propertyScrollPane);
        splitPane.setBottomComponent(layerScrollPane);

        panel.add(splitPane, BorderLayout.EAST);
        panel.add(createStatusBar(), BorderLayout.PAGE_END);
        return panel;
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

    private void openFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // save last directory
            properties.setProperty(LAST_DIR, file.getParent());
            writeProperties();
            fileChooser.setCurrentDirectory(file.getParentFile());

            map = loadMap(file.getParent(), file.getName());
            if (map != null) {
                saveRecentFile(file);
            }
        }
    }

    private TiledMap loadMap(String folder, String name) {

        AssetManager assetManager = app.getAssetManager();
        map = null;
        try {
            assetManager.registerLocator(folder, MyFileLocator.class);
            map = (TiledMap) assetManager.loadAsset(name);
            // remove from cache, in case of reload
            assetManager.deleteFromCache(new AssetKey<>(name));

            layerView.setTiledMap(map);
            app.load(map);
        } catch (Exception e) {
            log.error("Failed to load {} {}", folder, name, e);
            JOptionPane.showMessageDialog(this, "Failed to load " + name, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            assetManager.unregisterLocator(folder, MyFileLocator.class);
        }

        layer = null;

        if (map != null) {
            this.setTitle(APP_NAME + " - " + name);
        } else {
            this.setTitle(APP_NAME);
        }
        this.propertyTable.setMap(map);

        return map;
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
                    loadMap(recentFile.getFolder(), recentFile.getName());
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

    public void onPick(Point tile, Vector2f pixel) {
        // TODO Auto-generated method stub
        if (layer == null) {
            return;
        }

        if (layer instanceof TileLayer) {
            TileLayer tileLayer = (TileLayer) layer;
            int x = tile.getX();
            int y = tile.getY();
            if (x >= 0 && x < tileLayer.getWidth() && y >= 0 && y < tileLayer.getHeight()) {
                Tile t = tileLayer.getTileAt(x, y);
                propertyTable.setTile(t);
            }
        }
    }
}
