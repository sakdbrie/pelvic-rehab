package com.pelvic.report.service;

import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Pattern;

/** 报表模块内部通用工具：日期解析/格式化、Map 值安全转换、脱敏与中文缺省值。 */
final class ReportSupport {

    static final String EMPTY = "—";

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ReportSupport() {
    }

    static LocalDate today() {
        return LocalDate.now();
    }

    static LocalDate defaultStartDate() {
        return LocalDate.now().minusDays(29);
    }

    static LocalDate defaultEndDate() {
        return LocalDate.now();
    }

    /** 校验并解析 yyyy-MM-dd */
    static LocalDate parseDate(String value, String label) {
        if (value == null || !DATE_PATTERN.matcher(value).matches()) {
            throw new BizException(label + "格式错误，应为 yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(value, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new BizException(label + "格式错误，应为 yyyy-MM-dd");
        }
    }

    static void checkRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BizException("开始日期不能晚于结束日期");
        }
    }

    static String format(LocalDate date) {
        return date == null ? EMPTY : date.format(DATE_FMT);
    }

    /** 任意日期/时间对象 → yyyy-MM-dd */
    static String dateStr(Object value) {
        LocalDate date = toLocalDate(value);
        return date == null ? EMPTY : date.format(DATE_FMT);
    }

    /** 任意日期对象 → yyyy-MM（用于出生年月） */
    static String monthStr(Object value) {
        LocalDate date = toLocalDate(value);
        return date == null ? EMPTY : date.format(DATE_FMT).substring(0, 7);
    }

    /** 任意时间对象 → yyyy-MM-dd HH:mm */
    static String dateTimeStr(Object value) {
        LocalDateTime time = toLocalDateTime(value);
        return time == null ? EMPTY : time.format(DATETIME_FMT);
    }

    /** 任意时间对象 → HH:mm */
    static String timeStr(Object value) {
        LocalDateTime time = toLocalDateTime(value);
        return time == null ? EMPTY : time.format(TIME_FMT);
    }

    static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String text = String.valueOf(value).trim();
        if (text.length() >= 10) {
            try {
                return LocalDate.parse(text.substring(0, 10), DATE_FMT);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
        return null;
    }

    static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }
        if (value instanceof java.util.Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    /** 计算周岁 */
    static Integer ageOf(Object birth) {
        LocalDate date = toLocalDate(birth);
        if (date == null) {
            return null;
        }
        return Period.between(date, LocalDate.now()).getYears();
    }

    static String str(Object value) {
        if (value == null) {
            return EMPTY;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? EMPTY : text;
    }

    /** 文本字段：null/空白 → 缺省值 */
    static String text(Object value) {
        return str(value);
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    static long longValue(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static Long longVal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static int intValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** BigDecimal 去掉多余 0，避免 “60.00” 这类展示 */
    static String plain(Object value) {
        if (value == null) {
            return EMPTY;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? EMPTY : text;
    }

    static String withUnit(Object value, String unit) {
        if (value == null || String.valueOf(value).isBlank()) {
            return EMPTY;
        }
        return plain(value) + unit;
    }

    static boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = String.valueOf(value).trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "是".equals(text) || "有".equals(text);
    }

    /** tinyint 布尔 → 是/否 */
    static String yesNo(Object value) {
        if (value == null) {
            return EMPTY;
        }
        return truthy(value) ? "是" : "否";
    }

    /** tinyint 布尔 → 有/无 */
    static String hasOrNot(Object value) {
        if (value == null) {
            return EMPTY;
        }
        return truthy(value) ? "有" : "无";
    }

    static String phone(Object cipher, AesEncryptor aesEncryptor, Logger log) {
        if (cipher == null) {
            return "****";
        }
        try {
            String phone = aesEncryptor.decrypt(String.valueOf(cipher));
            if (phone != null && phone.length() == 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
        } catch (Exception e) {
            log.warn("手机号解密失败（请检查 pelvic.encrypt.aes-key 与存量数据密钥一致性）");
        }
        return "****";
    }
}
