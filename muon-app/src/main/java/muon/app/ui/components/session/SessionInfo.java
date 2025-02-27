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

public class SessionInfo extends NamedItem implements Serializable {

    @Setter
    @Getter
    private String host;

    @Setter
    @Getter
    private String user;

    @Setter
    @Getter
    private String localFolder;

    @Setter
    @Getter
    private String remoteFolder;

    @Setter
    @Getter
    private int port = 22;

    @Setter
    @Getter
    private List<String> favouriteRemoteFolders = new ArrayList<>();

    @Setter
    @Getter
    private List<String> favouriteLocalFolders = new ArrayList<>();

    @Setter
    @Getter
    private String privateKeyFile;

    @Setter
    @Getter
    private int proxyPort = 8080;

    @Setter
    @Getter
    private String proxyHost;

    @Setter
    @Getter
    private String proxyUser;

    @Setter
    @Getter
    private String proxyPassword;

    @Setter
    @Getter
    private int proxyType = 0;
    @Setter
    @Getter
    private boolean useJumpHosts = false;
    @Setter
    @Getter
    private JumpType jumpType = JumpType.TCP_FORWARDING;
    @Setter
    @Getter
    private List<HopEntry> jumpHosts = new ArrayList<>();
    @Setter
    @Getter
    private List<PortForwardingRule> portForwardingRules = new ArrayList<>();

    @Setter
    @Getter
    private boolean useX11Forwarding = false;

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
