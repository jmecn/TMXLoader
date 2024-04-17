package io.github.jmecn.tiled.app.jme3;

import com.jme3.asset.*;

import java.io.*;

/**
 * Enable to locate assets from file system. The asset cat locate outside the root path.
 *
 * @author yanmaoyuan
 */
public class MyFileLocator implements AssetLocator {

    private File root;

    @Override
    public void setRootPath(String rootPath) {
        if (rootPath == null) {
            throw new IllegalArgumentException("rootPath cannot be null");
        }

        try {
            root = new File(rootPath).getCanonicalFile();
            if (!root.isDirectory()){
                throw new IllegalArgumentException("Given root path \"" + root + "\" is not a directory");
            }
        } catch (IOException ex) {
            throw new AssetLoadException("Root path is invalid", ex);
        }
    }

    private static class AssetInfoFile extends AssetInfo {

        private final File file;

        public AssetInfoFile(AssetManager manager, AssetKey<?> key, File file){
            super(manager, key);
            this.file = file;
        }

        @Override
        public InputStream openStream() {
            try{
                return new FileInputStream(file);
            }catch (FileNotFoundException ex){
                throw new AssetLoadException("Failed to open file: " + file, ex);
            }
        }
    }

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        File file = new File(root, name);
        if (file.exists() && file.isFile()){
            return new AssetInfoFile(manager, key, file);
        }else{
            return null;
        }
    }
}
