package muon.app.ui.components.session.files.local;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileSystem;
import muon.app.common.local.LocalFileSystem;
import muon.app.util.OptionPaneUtils;
import muon.app.util.PathUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class LocalFileOperations {
    public boolean rename(String oldName, String newName) {
        try {
            Files.move(Paths.get(oldName), Paths.get(newName));
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean newFile(String folder) {
        String text = OptionPaneUtils.showInputDialog(null, App.getCONTEXT().getBundle().getString("new_file"), App.getCONTEXT().getBundle().getString("new_file"));
        if (text == null || text.isEmpty()) {
            return false;
        }
        LocalFileSystem fs = new LocalFileSystem();
        try {
            fs.createFile(PathUtils.combine(folder, text, File.separator));
            return true;
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("unable_create_file"));
        }
        return false;
    }

    public boolean newFolder(String folder) {
        String text = OptionPaneUtils.showInputDialog(null, App.getCONTEXT().getBundle().getString("new_folder_name"), App.getCONTEXT().getBundle().getString("new_folder_name"));
        if (text == null || text.isEmpty()) {
            return false;
        }
        FileSystem fs = new LocalFileSystem();
        try {
            fs.mkdir(PathUtils.combine(folder, text, fs.getSeparator()));
            return true;
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("unable_create_folder"));
        }
        return false;
    }
}
