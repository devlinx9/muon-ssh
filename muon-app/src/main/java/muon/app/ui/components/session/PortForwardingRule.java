package muon.app.ui.components.session;

import lombok.Getter;
import lombok.Setter;

/*
 * Port forwarding rule, meaning of host, sourcePort and targetPort changes depending on the type of port forwarding
 */
@Setter
@Getter
public class PortForwardingRule {

    private PortForwardingType type;
    private String host;
    private String bindHost;
    private int sourcePort;
    private int targetPort;

    /**
     * @param type       Local or remote
     * @param host       In case of local port forwarding, the destination host,
     *                   which is accessible from remote server, in case of remote
     *                   port forwarding this will be the network interface address
     *                   of the service running on local system
     * @param sourcePort Local port to bind for local port forwarding, for remote
     *                   this is the port bind on remote server
     * @param targetPort For local port forwarding this is the target port, for
     *                   remote this will be the local port of that local service
     */
    public PortForwardingRule(PortForwardingType type, String host, int sourcePort, int targetPort, String bindHost) {
        super();
        this.type = type;
        this.host = host;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.bindHost = bindHost;
    }

    public PortForwardingRule() {
    }

    public enum PortForwardingType {
        LOCAL, REMOTE
    }
}
