package io.github.jmecn.tiled.app;

import com.jme3.system.awt.AwtPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

    private static final String[] assets = {
            "image.tmx",
            "BeatBoss/forest.tmx",
            "BeatBoss/cave.tmx",
            "BeatBoss/tomb.tmx",

            "Orthogonal/01.tmx",
            "Orthogonal/02.tmx",
            "Orthogonal/03.tmx",
            "Orthogonal/04.tmx",
            "Orthogonal/05.tmx",
            "Orthogonal/06.tmx",
            "Orthogonal/07.tmx",
            "Orthogonal/orthogonal-outside.tmx",
            "Orthogonal/perspective_walls.tmx",
            "csvmap.tmx",
            "sewers.tmx",
            "Desert/desert.tmx",

            "Isometric/01.tmx",
            "Isometric/02.tmx",
            "Isometric/03.tmx",
            "Isometric/isometric_grass_and_water.tmx",

            "Hexagonal/01.tmx",
            "Hexagonal/02.tmx",
            "Hexagonal/03.tmx",
            "Hexagonal/04.tmx",
            "Hexagonal/05.tmx",
            "Hexagonal/hexagonal-mini.tmx",

            "Staggered/01.tmx",
            "Staggered/02.tmx",
            "Staggered/03.tmx",
            "Staggered/04.tmx",
            "Staggered/05.tmx", };

    private static final String[] names = {
            "image",
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
        for (String name : names) {
            model.addElement(name);
        }

        list.setModel(model);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                load(assets[list.getSelectedIndex()]);
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

    public void setMapStatus(String status) {
        mapStatus.setText(status);
    }

    public void setCursorStatus(String status) {
        cursorStatus.setText(status);
    }

    private void showLayers() {
        // TODO
    }
    private void load(String assetPath) {
        app.load(assetPath);
    }
}
