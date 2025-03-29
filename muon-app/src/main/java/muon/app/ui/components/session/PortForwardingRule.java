package muon.app.ui.components.session;

import lombok.Getter;
import lombok.Setter;
import muon.app.util.enums.PortForwardingType;

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
}
