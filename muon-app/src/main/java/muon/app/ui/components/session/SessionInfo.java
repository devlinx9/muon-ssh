package muon.app.ui.components.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import muon.app.util.enums.JumpType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class SessionInfo extends NamedItem implements Serializable {

    private String host;
    private String user;
    private String localFolder;
    private String remoteFolder;
    private int port = 22;
    private List<String> favouriteRemoteFolders = new ArrayList<>();
    private List<String> favouriteLocalFolders = new ArrayList<>();
    private String privateKeyFile;
    private int proxyPort = 8080;
    private String proxyHost;
    private String proxyUser;
    private String proxyPassword;
    private int proxyType = 0;
    private boolean useJumpHosts = false;
    private JumpType jumpType = JumpType.TCP_FORWARDING;
    private List<HopEntry> jumpHosts = new ArrayList<>();
    private List<PortForwardingRule> portForwardingRules = new ArrayList<>();
    private boolean useX11Forwarding = false;
    private boolean sftpOnly = false;

    private String password;

    /**
     * @return the password
     */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public SessionInfo copy() {
        SessionInfo info = new SessionInfo();
        info.setId(UUID.randomUUID().toString());
        info.setHost(this.host);
        info.setPort(this.port);
        info.getFavouriteRemoteFolders().addAll(favouriteRemoteFolders);
        info.getFavouriteLocalFolders().addAll(favouriteLocalFolders);
        info.setLocalFolder(this.localFolder);
        info.setRemoteFolder(this.remoteFolder);
        info.setPassword(this.password);
        info.setPrivateKeyFile(privateKeyFile);
        info.setUser(user);
        info.setName(name);
        info.setUseX11Forwarding(useX11Forwarding);
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionInfo that = (SessionInfo) o;
        return Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, user, localFolder, remoteFolder, port, favouriteRemoteFolders, favouriteLocalFolders, privateKeyFile, proxyPort, proxyHost, proxyUser, proxyPassword, proxyType, useJumpHosts, jumpType
                , jumpHosts, portForwardingRules, password, useX11Forwarding);
    }


}
