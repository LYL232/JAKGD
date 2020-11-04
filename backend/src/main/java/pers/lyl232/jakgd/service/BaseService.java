package pers.lyl232.jakgd.service;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class BaseService {
    final static private DateTimeFormatter secondFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    final static private DateTimeFormatter monthFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.CHINA);

    /**
     * 获取当前上海时间精确到秒的时间字符串
     *
     * @return 字符串
     */
    public String getFormatNowDateStringBySecond() {
        return secondFormatter.format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 获取当前上海时间精确到月的时间字符串
     *
     * @return 字符串
     */
    public String getFormatNowDateStringByMonth() {
        return monthFormatter.format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
    }

}
