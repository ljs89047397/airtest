package kr.go.molit.icas.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final DateTimeFormatter DATETIME_FORMAT  = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    public static final DateTimeFormatter YMD              = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtil() { }

    public static String formatDate(LocalDate d) {
        return d == null ? "" : d.format(DATE_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME_FORMAT);
    }

    public static String formatDateTime(java.sql.Timestamp ts) {
        return ts == null ? "" : ts.toLocalDateTime().format(DATETIME_FORMAT);
    }

    public static LocalDate parseYmd(String ymd) {
        return ymd == null || ymd.isBlank() ? null : LocalDate.parse(ymd, YMD);
    }
}
