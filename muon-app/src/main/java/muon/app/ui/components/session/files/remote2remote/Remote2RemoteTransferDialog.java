package muon.app.ui.components.session.files.remote2remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.dialog.NewSessionDlg;
import muon.app.util.Constants;
import muon.app.util.FontAwesomeContants;
import muon.app.util.OptionPaneUtils;
import muon.app.util.enums.FileType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class Remote2RemoteTransferDialog extends JDialog {
    public static final String NEW_LINE_STR = "\"\n";
    private final DefaultListModel<RemoteServerEntry> remoteHostModel;
    private final JList<RemoteServerEntry> remoteHostList;
    private final SessionContentPanel session;
    private final FileInfo[] selectedFiles;
    private final String currentDirectory;
    private final List<RemoteServerEntry> list = new ArrayList<>();

    public Remote2RemoteTransferDialog(JFrame frame, SessionContentPanel session, FileInfo[] selectedFiles,
                                       String currentDirectory) {
        super(frame);
        setTitle("Server to server SFTP");
        this.session = session;
        this.selectedFiles = selectedFiles;
        this.currentDirectory = currentDirectory;
        setSize(640, 480);
        setModal(true);

        remoteHostModel = new DefaultListModel<>();
        this.list.clear();
        this.list.addAll(load());
        remoteHostModel.addAll(this.list);
        remoteHostList = new JList<>(remoteHostModel);
        remoteHostList.setCellRenderer(new RemoteHostRenderer());

        remoteHostList.setBackground(App.getCONTEXT().getSkin().getTextFieldBackground());

        SkinnedScrollPane scrollPane = new SkinnedScrollPane(remoteHostList);
        scrollPane.setBorder(new MatteBorder(0, 0, 1, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()));

        this.add(scrollPane);
        if (!remoteHostModel.isEmpty()) {
            remoteHostList.setSelectedIndex(0);
        }

        Box bottom = Box.createHorizontalBox();
        JButton btnAddKnown = new JButton(App.getCONTEXT().getBundle().getString("add_from_manager"));
        JButton btnAdd = new JButton(App.getCONTEXT().getBundle().getString("add"));
        JButton btnRemove = new JButton(App.getCONTEXT().getBundle().getString("delete"));
        JButton btnEdit = new JButton(App.getCONTEXT().getBundle().getString("edit"));
        JButton btnSend = new JButton(App.getCONTEXT().getBundle().getString("send_files"));

        btnAddKnown.addActionListener(e -> {
            SessionInfo info = new NewSessionDlg(this).newSession();
            if (info != null) {
                RemoteServerEntry ent = getEntryDetails(info.getHost(), info.getUser(), info.getRemoteFolder(),
                                                        info.getPort());
                if (ent != null) {
                    remoteHostModel.insertElementAt(ent, 0);
                    remoteHostList.setSelectedIndex(0);
                    save();
                }
            }
        });

        btnAdd.addActionListener(e -> {
            RemoteServerEntry ent = getEntryDetails(null, null, null, 22);
            if (ent != null) {
                remoteHostModel.insertElementAt(ent, 0);
                remoteHostList.setSelectedIndex(0);
                save();
            }
        });

        btnEdit.addActionListener(e -> {
            int index = remoteHostList.getSelectedIndex();
            if (index != -1) {
                RemoteServerEntry ent = remoteHostModel.get(index);
                RemoteServerEntry ent2 = getEntryDetails(ent.getHost(), ent.getUser(), ent.getPath(), ent.getPort());
                if (ent2 != null) {
                    ent.setHost(ent2.getHost());
                    ent.setUser(ent2.getUser());
                    ent.setPath(ent2.getPath());
                    ent.setPort(ent2.getPort());
                    save();
                }
            }
        });

        btnRemove.addActionListener(e -> {
            int index = remoteHostList.getSelectedIndex();
            if (index != -1) {
                remoteHostModel.remove(index);
                save();
            }
        });

        btnSend.addActionListener(e -> {
            int index = remoteHostList.getSelectedIndex();
            if (index != -1) {
                RemoteServerEntry ent = remoteHostModel.get(index);
                this.dispose();
                this.session.openTerminal(generateCommand(ent));
            }
        });

        bottom.add(btnAddKnown);
        bottom.add(Box.createHorizontalStrut(5));
        bottom.add(btnAdd);
        bottom.add(Box.createHorizontalStrut(5));
        bottom.add(btnEdit);
        bottom.add(Box.createHorizontalStrut(5));
        bottom.add(btnRemove);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(btnSend);
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(bottom, BorderLayout.SOUTH);

        Box top = Box.createHorizontalBox();
        JLabel lblSearch = new JLabel(FontAwesomeContants.FA_SEARCH);
        lblSearch.setFont(App.getCONTEXT().getSkin().getIconFont());
        JTextField txtSearch = new SkinnedTextField(30);
        txtSearch.setBackground(App.getCONTEXT().getSkin().getDefaultBackground());
        txtSearch.setBorder(new EmptyBorder(10, 10, 10, 10));
        top.add(lblSearch);
        top.add(txtSearch);
        top.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 10, 10),
                                         new MatteBorder(0, 0, 1, 0, App.getCONTEXT().getSkin().getDefaultSelectionBackground())));

        this.add(top, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterItems(txtSearch.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterItems(txtSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterItems(txtSearch.getText());
            }
        });
    }

    private void filterItems(String filter) {
        this.remoteHostModel.removeAllElements();
        for (RemoteServerEntry ent : this.list) {
            if (ent.getHost().contains(filter) || ent.getPath().contains(filter)) {
                this.remoteHostModel.addElement(ent);
            }
        }
    }

    private RemoteServerEntry getEntryDetails(String host, String user, String path, int port) {
        JTextField txtHost = new SkinnedTextField(30);
        JTextField txtUser = new SkinnedTextField(30);
        JTextField txtPath = new SkinnedTextField(30);
        JSpinner spPort = new JSpinner(new SpinnerNumberModel(port, 1, 65535, 1));

        if (host != null) {
            txtHost.setText(host);
        }
        if (user != null) {
            txtUser.setText(user);
        }
        if (path != null) {
            txtPath.setText(path);
        }
        if (port > 0) {
            spPort.setValue(port);
        }

        while (OptionPaneUtils.showOptionDialog(this,
                                                new Object[]{"Host", txtHost, "User", txtUser, "Copy to ( target directory)", txtPath, "Port",
                                                             spPort},
                                                "Remote host details") == JOptionPane.OK_OPTION) {
            host = txtHost.getText();
            user = txtUser.getText();
            path = txtPath.getText();
            port = (Integer) spPort.getValue();
            if (host.isEmpty() || user.isEmpty() || path.isEmpty() || port <= 0) {
                JOptionPane.showMessageDialog(this, App.getCONTEXT().getBundle().getString("invalid_input"));
                continue;
            }
            return new RemoteServerEntry(host, port, user, path);
        }
        return null;
    }

    private String generateCommand(RemoteServerEntry e) {
        return createSftpFileList(e);
    }

    private String createSftpFileList(RemoteServerEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append("sftp ").append(e.getUser()).append("@").append(e.getHost()).append("<<EOF\n");
        sb.append("lcd \"").append(this.currentDirectory).append(NEW_LINE_STR);
        sb.append("cd \"").append(e.getPath()).append(NEW_LINE_STR);

        for (FileInfo finfo : selectedFiles) {
            if (finfo.getType() == FileType.DIRECTORY) {
                sb.append("mkdir \"").append(finfo.getName()).append(NEW_LINE_STR);
                sb.append("put -r \"").append(finfo.getName()).append(NEW_LINE_STR);
            } else if (finfo.getType() == FileType.FILE) {
                sb.append("put -P \"").append(finfo.getName()).append(NEW_LINE_STR);
            }
        }
        sb.append("bye\n");
        sb.append("EOF\n");
        return sb.toString();
    }

    private void save() {
        List<RemoteServerEntry> list = new ArrayList<>();
        for (int i = 0; i < remoteHostModel.size(); i++) {
            list.add(remoteHostModel.get(i));
        }
        File file = new File(App.getCONTEXT().getConfigDir(), Constants.TRANSFER_HOSTS);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, list);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        this.list.clear();
        this.list.addAll(load());
    }

    private List<RemoteServerEntry> load() {
        File file = new File(App.getCONTEXT().getConfigDir(), Constants.TRANSFER_HOSTS);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, new TypeReference<>() {
                });
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }
}
