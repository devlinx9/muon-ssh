package muon.app.util;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Slf4j
public class ScriptLoader {
    public static synchronized String loadShellScript(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(ScriptLoader.class.getResourceAsStream(path))))) {
                while (true) {
                    String s = r.readLine();
                    if (s == null) {
                        break;
                    }
                    sb.append(s).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
