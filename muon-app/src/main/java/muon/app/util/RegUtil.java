/**
 *
 */
package muon.app.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subhro
 */
@Slf4j
public class RegUtil {
    public static String regGetStr(WinReg.HKEY hkey, String key,
                                   String value) {
        try {
            return Advapi32Util.registryGetStringValue(hkey, key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static int regGetInt(WinReg.HKEY hkey, String key, String value) {
        try {
            return Advapi32Util.registryGetIntValue(hkey, key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }
}
