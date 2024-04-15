package io.github.jmecn.tiled.app;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.system.awt.AwtPanel;
import io.github.jmecn.tiled.core.TiledMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class MainWnd extends JFrame {

    static Logger log = LoggerFactory.getLogger(MainWnd.class.getName());

    private final transient TiledApp app;

    private JLabel mapStatus;
    private JLabel cursorStatus;
    private JFileChooser fileChooser;

    public MainWnd(TiledApp app, AwtPanel awtPanel) {
        super("Tiled Map Viewer");

        this.app = app;

        setSize(800, 600);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                app.stop();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(awtPanel, BorderLayout.CENTER);

        this.fileChooser = createFileChooser();
        // menu
        this.setJMenuBar(createMenuBar());

        this.getContentPane().add(new JScrollPane(createList()), BorderLayout.WEST);

        this.getContentPane().add(createStatusBar(), BorderLayout.PAGE_END);

        this.pack();

        /*
         * center
         */
        center();
    }

    private JList<String> createList() {
        JList<String> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultListModel<String> model = new DefaultListModel<>();
//        for (String name : names) {
//            model.addElement(name);
//        }

        list.setModel(model);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // load(assets[list.getSelectedIndex()]);
            }
        });

        return list;
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

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setMnemonic('S');
        saveItem.setEnabled(false);
        fileMenu.add(saveItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(e -> dispose());
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

    private JPanel createStatusBar() {
        this.mapStatus = new JLabel("Map: None");
        this.cursorStatus = new JLabel("Ready.");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(mapStatus);
        panel.add(new JSeparator(JSeparator.VERTICAL));
        panel.add(cursorStatus);

        return panel;
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().endsWith(".tmx");
            }

            @Override
            public String getDescription() {
                return "Tiled Map Files (*.tmx)";
            }
        });

        return fileChooser;
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

    private void load() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            fileChooser.setCurrentDirectory(file.getParentFile());
            String fileName = file.getName();

            AssetManager assetManager = app.getAssetManager();
            log.info("Load:{}", file.getAbsoluteFile());
            TiledMap map = null;
            try {
                assetManager.registerLocator(file.getParent(), FileLocator.class);
                map = (TiledMap) assetManager.loadAsset(fileName);
                // remove from cache, in case of reload
                assetManager.deleteFromCache(new AssetKey<>(fileName));
            } catch (Exception e) {
                log.error("Failed to load {}", file.getAbsoluteFile(), e);
            } finally {
                assetManager.unregisterLocator(file.getParent(), FileLocator.class);
            }

            if (map != null) {
                app.load(map);
                // update the window title
                this.setTitle("Tiled Map Viewer - " + fileName);
            }
        }
    }

    private void load(String assetPath) {
        app.load(assetPath);
    }
}
