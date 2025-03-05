package muon.app.ui.components.session.files.remote2remote;

import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ssh.CachedCredentialProvider;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.dialog.NewSessionDlg;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.util.OptionPaneUtils;
import muon.app.util.enums.ConflictAction;

public class LocalPipeTransfer {
    public void transferFiles(FileBrowser fileBrowser, String currentDirectory, FileInfo[] selectedFiles) {
        SessionInfo info = new NewSessionDlg(App.getAppWindow()).newSession();
        if (info != null) {
            String path = OptionPaneUtils.showInputDialog(null, App.getContext().getBundle().getString("remote_path"), App.getContext().getBundle().getString("remote_path"));
            if (path != null) {
                RemoteSessionInstance ri =
                        new RemoteSessionInstance(info, new CachedCredentialProvider() {
                            private String cachedPassword;
                            private String cachedPassPhrase;
                            private String cachedUser;

                            @Override
                            public synchronized String getCachedPassword() {
                                return cachedPassword;
                            }

                            @Override
                            public synchronized void cachePassword(String password) {
                                this.cachedPassword = password;
                            }

                            @Override
                            public synchronized String getCachedPassPhrase() {
                                return cachedPassPhrase;
                            }

                            @Override
                            public synchronized void setCachedPassPhrase(String cachedPassPhrase) {
                                this.cachedPassPhrase = cachedPassPhrase;
                            }

                            @Override
                            public synchronized String getCachedUser() {
                                return cachedUser;
                            }

                            @Override
                            public synchronized void setCachedUser(String cachedUser) {
                                this.cachedUser = cachedUser;
                            }
                        }, null);

                SshFileSystem sshFS = ri.getSshFs();
                fileBrowser.newFileTransfer(fileBrowser.getSSHFileSystem(), sshFS, selectedFiles, path, this.hashCode(),
                                            ConflictAction.PROMPT, null);
            }
        }
    }
}
