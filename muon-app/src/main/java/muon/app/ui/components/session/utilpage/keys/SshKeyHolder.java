package muon.app.ui.components.session.utilpage.keys;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SshKeyHolder {
    private String remotePublicKey;
    private String localPublicKey;
    private String remoteAuthorizedKeys;
    private String remotePubKeyFile;
    private String localPubKeyFile;

    public SshKeyHolder() {
    }

    public SshKeyHolder(String remotePublicKey, String localPublicKey,
                        String remoteAuthorizedKeys, String remotePubKeyFile,
                        String localPubKeyFile) {
        this.remotePublicKey = remotePublicKey;
        this.localPublicKey = localPublicKey;
        this.remoteAuthorizedKeys = remoteAuthorizedKeys;
        this.remotePubKeyFile = remotePubKeyFile;
        this.localPubKeyFile = localPubKeyFile;
    }

    @Override
    public String toString() {
        return "SshKeyHolder{" +
                "remotePublicKey='" + remotePublicKey + '\'' +
                ", localPublicKey='" + localPublicKey + '\'' +
                ", remoteAuthorizedKeys='" + remoteAuthorizedKeys + '\'' +
                ", remotePubKeyFile='" + remotePubKeyFile + '\'' +
                ", localPubKeyFile='" + localPubKeyFile + '\'' +
                '}';
    }
}
