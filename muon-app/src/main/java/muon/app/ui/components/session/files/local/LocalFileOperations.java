package muon.app.ui.components.session.files.local;

import muon.app.common.FileSystem;
import muon.app.common.local.LocalFileSystem;
import util.PathUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileOperations {
    public boolean rename(String oldName, String newName) {
        try {
            Files.move(Paths.get(oldName), Paths.get(newName));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean newFile(String folder) {
        String text = JOptionPane.showInputDialog("New file");
        if (text == null || text.length() < 1) {
            return false;
        }
        LocalFileSystem fs = new LocalFileSystem();
        try {
            fs.createFile(PathUtils.combine(folder, text, File.separator));
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            // TODO i18n
            JOptionPane.showMessageDialog(null, "Unable to create new file");
        }
        return false;
    }

    public boolean newFolder(String folder) {
        // TODO i18n
        String text = JOptionPane.showInputDialog("New folder name");
        if (text == null || text.length() < 1) {
            return false;
        }
        FileSystem fs = new LocalFileSystem();
        try {
            fs.mkdir(PathUtils.combine(folder, text, fs.getSeparator()));
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            // TODO i18n
            JOptionPane.showMessageDialog(null, "Unable to create new folder");
        }
        return false;
    }
}
