
package muon.app.util.importers;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.slf4j.Slf4j;
import muon.app.ui.components.session.NamedItem;
import muon.app.ui.components.session.SessionFolder;
import muon.app.ui.components.session.SessionInfo;
import muon.app.util.RegUtil;

import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author subhro
 */
@Slf4j
public class WinScpImporter {
    protected WinScpImporter() {

    }

    private static final String WIN_SCP_REG_KEY = "Software\\Martin Prikryl\\WinSCP 2\\Sessions";

    public static Map<String, String> getKeyNames() {
        Map<String, String> map = new HashMap<>();
        try {
            String[] keys = Advapi32Util
                    .registryGetKeys(WinReg.HKEY_CURRENT_USER, WIN_SCP_REG_KEY);
            for (String key : keys) {
                String decodedKey = key.replace("%20", " ");
                map.put(key, decodedKey);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        log.info(map.toString());

        return map;
    }

    public static void importSessions(DefaultMutableTreeNode node,
                                      List<String> keys) {

        for (String key : keys) {
            if (RegUtil.regGetInt(WinReg.HKEY_CURRENT_USER,
                                  WIN_SCP_REG_KEY + "\\" + key, "FSProtocol") == 0) {
                String host = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                WIN_SCP_REG_KEY + "\\" + key, "HostName");
                int port = RegUtil.regGetInt(WinReg.HKEY_CURRENT_USER,
                                             WIN_SCP_REG_KEY + "\\" + key, "PortNumber");
                if (port == 0) {
                    port = 22;
                }
                String user = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                WIN_SCP_REG_KEY + "\\" + key, "UserName");
                String keyfile = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                   WIN_SCP_REG_KEY + "\\" + key, "PublicKeyFile");

                String proxyHost = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                     WIN_SCP_REG_KEY + "\\" + key, "ProxyHost");
                int proxyPort = RegUtil.regGetInt(WinReg.HKEY_CURRENT_USER,
                                                  WIN_SCP_REG_KEY + "\\" + key, "ProxyPort");
                String proxyUser = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                     WIN_SCP_REG_KEY + "\\" + key, "ProxyUsername");

                String proxyPass = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                                     WIN_SCP_REG_KEY + "\\" + key, "ProxyPassword");

                int proxyType = RegUtil.regGetInt(WinReg.HKEY_CURRENT_USER,
                                                  WIN_SCP_REG_KEY + "\\" + key, "ProxyMethod");
                if (proxyType == 1) {
                    proxyType = 2;
                } else if (proxyType == 2) {
                    proxyType = 3;
                } else if (proxyType == 3) {
                    proxyType = 1;
                } else {
                    proxyType = 0;
                }

                SessionInfo info = new SessionInfo();

                info.setHost(host);
                info.setPort(port);
                info.setUser(user);
                if (keyfile != null && !keyfile.isEmpty()) {
                    info.setPrivateKeyFile(
                            URLDecoder.decode(keyfile, StandardCharsets.UTF_8));
                }
                info.setProxyHost(proxyHost);
                info.setProxyPort(proxyPort);
                info.setProxyUser(proxyUser);
                info.setProxyPassword(proxyPass);
                info.setProxyType(proxyType);

                try {

                    String[] arr = URLDecoder.decode(key, StandardCharsets.UTF_8).split("/");
                    info.setName(arr[arr.length - 1]);

                    DefaultMutableTreeNode parent = node;

                    if (arr.length > 1) {
                        for (int i = 0; i < arr.length - 1; i++) {

                            DefaultMutableTreeNode parent2 = find(parent,
                                                                  arr[i]);
                            if (parent2 == null) {
                                SessionFolder folder = new SessionFolder();
                                folder.setName(arr[i]);
                                parent2 = new DefaultMutableTreeNode(folder);
                                parent2.setAllowsChildren(true);
                                parent.add(parent2);
                            }

                            parent = parent2;
                        }

                    }

                    DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(
                            info);
                    node1.setAllowsChildren(false);
                    parent.add(node1);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static DefaultMutableTreeNode find(DefaultMutableTreeNode node,
                                               String name) {
        NamedItem item = (NamedItem) node.getUserObject();
        if (item.getName().equals(name)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
                    .getChildAt(i);
            if (child.getAllowsChildren()) {
                DefaultMutableTreeNode fn = find(child, name);
                if (fn != null) {
                    return fn;
                }
            }
        }
        return null;
    }
}
