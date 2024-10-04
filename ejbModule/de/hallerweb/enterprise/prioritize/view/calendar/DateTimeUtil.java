package de.hallerweb.enterprise.prioritize.view.calendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    private DateTimeUtil() {
        super();
    }

    public static final LocalDateTime toLocalDateTime(Date d) {
        return d.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static final LocalDateTime toLocalDateTime(Calendar cal) {
        return cal.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static final Date toDate(LocalDateTime d) {
        return java.util.Date
                .from(d.atZone(ZoneId.systemDefault())
                        .toInstant());
    }

}
