package muon.app.util;

import com.sun.jna.platform.FileMonitor;
import com.sun.jna.platform.win32.W32FileMonitor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public final class Win32DragHandler {
    private final FileMonitor fileMonitor = new W32FileMonitor();

    public synchronized void listenForDrop(String keyToListen, Consumer<File> callback) {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (File drive : File.listRoots()) {
            if (fsv.isDrive(drive)) {
                try {
                    log.info("Adding to watch: {}", drive.getAbsolutePath());
                    fileMonitor.addWatch(drive, W32FileMonitor.FILE_RENAMED | W32FileMonitor.FILE_CREATED, true);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        fileMonitor.addFileListener(e -> {
            File file = e.getFile();
            log.info(file.getAbsolutePath());
            if (file.getName().startsWith(keyToListen)) {
                callback.accept(file);
            }
        });
    }

    public synchronized void dispose() {
        log.info("File watcher disposed");
        this.fileMonitor.dispose();
    }
}
