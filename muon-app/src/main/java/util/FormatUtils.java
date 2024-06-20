package util;

import muon.app.App;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FormatUtils {
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyyMMdd");

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (si ? "" : "i");
        return String.format("%.1f %s", bytes / Math.pow(unit, exp), pre);
    }

    public static String formatDate(LocalDateTime dateTime) {

        if (App.getGlobalSettings().isShowActualDateOnlyHour()) {
            Date actualDate = java.util.Date
                    .from(LocalDateTime.now().atZone(ZoneId.systemDefault())
                            .toInstant());

            Date objectDate = java.util.Date
                    .from(dateTime.atZone(ZoneId.systemDefault())
                            .toInstant());

            if (FMT.format(actualDate).equals(FMT.format(objectDate))) {
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }

        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
    }
}
