package muon.app.common.local;

import lombok.extern.slf4j.Slf4j;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.common.InputTransferChannel;
import muon.app.common.OutputTransferChannel;
import muon.app.util.PathUtils;
import muon.app.util.enums.FileType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class LocalFileSystem implements FileSystem {
    public static final String PROTO_LOCAL_FILE = "local";

    public void chmod(int perm, String path) {
    }

    @Override
    public FileInfo getInfo(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotFoundException(path);
        }
        Path p = f.toPath();
        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
        return new FileInfo(f.getName(), path, f.length(),
                f.isDirectory() ? FileType.DIRECTORY : FileType.FILE, f.lastModified(), -1, PROTO_LOCAL_FILE, "",
                attrs.creationTime().toMillis(), "", f.isHidden());
    }

    @Override
    public String getHome() {
        return System.getProperty("user.home");
    }

    @Override
    public List<FileInfo> list(String path) throws Exception {
        if (path == null || path.isEmpty()) {
            path = System.getProperty("user.home");
        }
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File[] childs = new File(path).listFiles();
        List<FileInfo> list = new ArrayList<>();
        if (childs == null || childs.length < 1) {
            return list;
        }
        for (File f : childs) {
            try {
                Path p = f.toPath();
                FileOwnerAttributeView ownerView = Files.getFileAttributeView(p, FileOwnerAttributeView.class);
                UserPrincipal owner = ownerView.getOwner();

                long creationTime;
                String permissionString = "";
                if (Files.getFileStore(p).supportsFileAttributeView(PosixFileAttributeView.class)) {
                    PosixFileAttributes readAttributes = Files.readAttributes(p, PosixFileAttributes.class);
                    Set<PosixFilePermission> permissions = readAttributes.permissions();
                    permissionString = PosixFilePermissions.toString(permissions);
                    creationTime = readAttributes.creationTime().toMillis();
                } else {
                    log.warn("POSIX file attribute view not supported.");
                    BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                    creationTime = attrs.creationTime().toMillis();
                }

                FileInfo info = new FileInfo(f.getName(), f.getAbsolutePath(), f.length(),
                        f.isDirectory() ? FileType.DIRECTORY : FileType.FILE, f.lastModified(), -1, PROTO_LOCAL_FILE,
                        permissionString, creationTime, "", f.isHidden());
                info.setUser(owner.getName());
                list.add(info);
            } catch (Exception e) {
                log.error("Error getting the metadata of the local file", e);
            }
        }
        return list;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    public InputStream getInputStream(String file, long offset) throws Exception {
        FileInputStream fout = new FileInputStream(file);
        fout.skip(offset);
        return fout;
    }

    public OutputStream getOutputStream(String file) throws Exception {
        return new FileOutputStream(file);
    }

    @Override
    public void rename(String oldName, String newName) throws Exception {
        log.info("Renaming from {} to: {}", oldName, newName);
        if (!new File(oldName).renameTo(new File(newName))) {
            throw new FileNotFoundException();
        }
    }

    public synchronized void delete(FileInfo f) throws Exception {
        if (f.getType() == FileType.DIRECTORY) {
            List<FileInfo> list = list(f.getPath());
            if (list != null && !list.isEmpty()) {
                for (FileInfo fc : list) {
                    delete(fc);
                }
            }
        }
        new File(f.getPath()).delete();
    }

    @Override
    public void mkdir(String path) throws Exception {
        log.info("Creating folder: {}", path);
        new File(path).mkdirs();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean mkdirs(String absPath) {
        return new File(absPath).mkdirs();
    }

    @Override
    public long getAllFiles(String dir, String baseDir, Map<String, String> fileMap, Map<String, String> folderMap)
            throws Exception {
        long size = 0;
        log.info("get files: {}", dir);
        String parentFolder = PathUtils.combineUnix(baseDir, PathUtils.getFileName(dir));

        folderMap.put(dir, parentFolder);

        List<FileInfo> list = list(dir);
        for (FileInfo f : list) {
            if (f.getType() == FileType.DIRECTORY) {
                folderMap.put(f.getPath(), PathUtils.combineUnix(parentFolder, f.getName()));
                size += getAllFiles(f.getPath(), parentFolder, fileMap, folderMap);
            } else {
                fileMap.put(f.getPath(), PathUtils.combineUnix(parentFolder, f.getName()));
                size += f.getSize();
            }
        }
        return size;
    }

    /*
     * (non-Javadoc)
     *
     * @see nixexplorer.core.FileSystemProvider#deleteFile(java.lang.String)
     */
    @Override
    public void deleteFile(String f) {
        new File(f).delete();
    }

    @Override
    public String getProtocol() {
        return PROTO_LOCAL_FILE;
    }

    /*
     * (non-Javadoc)
     *
     * @see nixexplorer.core.FileSystemProvider#createFile(java.lang.String)
     */
    @Override
    public void createFile(String path) throws Exception {
        Files.createFile(Paths.get(path));
    }

    public void createLink(String src, String dst, boolean hardLink) {

    }

    @Override
    public String getName() {
        return "Local files";
    }

    @Override
    public String[] getRoots() {
        File[] roots = File.listRoots();
        String[] arr = new String[roots.length];
        int i = 0;
        for (File f : roots) {
            arr[i++] = f.getAbsolutePath();
        }
        return arr;
    }

    public InputTransferChannel inputTransferChannel() throws Exception {
        return new InputTransferChannel() {
            @Override
            public InputStream getInputStream(String path) throws Exception {
                return new FileInputStream(path);
            }

            @Override
            public String getSeparator() {
                return File.separator;
            }

            @Override
            public long getSize(String path) throws Exception {
                return getInfo(path).getSize();
            }

        };
    }

    public OutputTransferChannel outputTransferChannel() throws Exception {
        return new OutputTransferChannel() {
            @Override
            public OutputStream getOutputStream(String path) throws Exception {
                return new FileOutputStream(path);
            }

            @Override
            public String getSeparator() {
                return File.separator;
            }
        };
    }

    public String getSeparator() {
        return File.separator;
    }
}
