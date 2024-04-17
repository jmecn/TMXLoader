package io.github.jmecn.tiled.app.swing;

import java.io.File;

/**
 * Record the recent files.
 *
 * @author yanmaoyuan
 */
public class RecentFile {
    private final String name;
    private final String folder;
    private final String absolutePath;

    public RecentFile(File file) {
        this.name = file.getName();
        this.folder = file.getParent();
        this.absolutePath = file.getAbsolutePath();
    }

    public RecentFile(String absolutePath) {
        this(new File(absolutePath));
    }

    public RecentFile(String name, String folder) {
        this.name = name;
        this.folder = folder;
        this.absolutePath = folder + File.separator + name;
    }

    public String getName() {
        return name;
    }

    public String getFolder() {
        return folder;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public String toString() {
        return folder + File.separator + name;
    }
}
