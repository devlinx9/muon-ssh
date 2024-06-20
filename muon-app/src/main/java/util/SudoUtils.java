package util;

import muon.app.ssh.RemoteSessionInstance;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class SudoUtils {
    private static final JPasswordField PASSWORD_FIELD = new JPasswordField(30);

    public static int runSudo(String command, RemoteSessionInstance instance, String password) {
        String prompt = UUID.randomUUID().toString();
        try {
            AtomicBoolean firstTime = new AtomicBoolean(true);
            String fullCommand = "sudo -S -p '" + prompt + "' " + command;
            System.out.println(
                    "Full sudo: " + fullCommand + "\nprompt: " + prompt);
            int ret = instance.exec(fullCommand, cmd -> {
                try {
                    InputStream in = cmd.getInputStream();
                    OutputStream out = cmd.getOutputStream();
                    StringBuilder sb = new StringBuilder();
                    Reader r = new InputStreamReader(in,
                            StandardCharsets.UTF_8);

                    char[] b = new char[8192];

                    while (cmd.isOpen()) {
                        int x = r.read(b);
                        if (x > 0) {
                            sb.append(b, 0, x);
                        }

                        System.out.println("buffer: " + sb);
                        if (sb.indexOf(prompt) != -1) {
                            if (firstTime.get() || JOptionPane.showOptionDialog(null,
                                    new Object[]{"User password",
                                            PASSWORD_FIELD},
                                    "Authentication",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE, null, null,
                                    null) == JOptionPane.OK_OPTION) {
                                if (firstTime.get()) {
                                    firstTime.set(false);
                                    PASSWORD_FIELD.setText(password);

                                }
                                sb = new StringBuilder();
                                out.write(
                                        (new String(PASSWORD_FIELD.getPassword())
                                                + "\n").getBytes());
                                out.flush();
                            } else {
                                cmd.close();
                                return -2;
                            }
                        }
                        Thread.sleep(50);
                    }
                    cmd.join();
                    cmd.close();
                    return cmd.getExitStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }, true);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int runSudo(String command, RemoteSessionInstance instance) {
        String prompt = UUID.randomUUID().toString();
        try {
            String fullCommand = "sudo -S -p '" + prompt + "' " + command;
            System.out.println(
                    "Full sudo: " + fullCommand + "\nprompt: " + prompt);
            int ret = instance.exec(fullCommand, cmd -> {
                try {
                    InputStream in = cmd.getInputStream();
                    OutputStream out = cmd.getOutputStream();
                    StringBuilder sb = new StringBuilder();
                    Reader r = new InputStreamReader(in,
                            StandardCharsets.UTF_8);

                    char[] b = new char[8192];

                    while (cmd.isOpen()) {
                        int x = r.read(b);
                        if (x > 0) {
                            sb.append(b, 0, x);
                        }

                        System.out.println("buffer: " + sb);
                        if (sb.indexOf(prompt) != -1) {
                            if (JOptionPane.showOptionDialog(null,
                                    new Object[]{"User password",
                                            PASSWORD_FIELD},
                                    "Authentication",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE, null, null,
                                    null) == JOptionPane.OK_OPTION) {
                                sb = new StringBuilder();
                                out.write(
                                        (new String(PASSWORD_FIELD.getPassword())
                                                + "\n").getBytes());
                                out.flush();
                            } else {
                                cmd.close();
                                return -2;
                            }
                        }
                        Thread.sleep(50);
                    }
                    cmd.join();
                    cmd.close();
                    return cmd.getExitStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }, true);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int runSudoWithOutput(String command,
                                        RemoteSessionInstance instance, StringBuilder output,
                                        StringBuilder error, String password) {
        String prompt = UUID.randomUUID().toString();
        try {
            String fullCommand = "sudo -S -p '" + prompt + "' " + command;
            System.out.println(
                    "Full sudo: " + fullCommand + "\nprompt: " + prompt);
            int ret = instance.exec(fullCommand, cmd -> {
                try {
                    InputStream in = cmd.getInputStream();
                    OutputStream out = cmd.getOutputStream();
                    StringBuilder sb = new StringBuilder();

                    Reader r = new InputStreamReader(in,
                            StandardCharsets.UTF_8);

                    while (true) {
                        int ch = r.read();
                        if (ch == -1)
                            break;
                        sb.append((char) ch);
                        output.append((char) ch);

                        System.out.println("buffer: " + sb);
                        if (sb.indexOf(prompt) != -1) {
                            sb = new StringBuilder();
                            out.write(
                                    (password
                                            + "\n").getBytes());
                            out.flush();
                        }

                    }
                    cmd.join();
                    cmd.close();
                    return cmd.getExitStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }, true);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
