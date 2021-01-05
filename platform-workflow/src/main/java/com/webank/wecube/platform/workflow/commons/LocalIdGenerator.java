package com.webank.wecube.platform.workflow.commons;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * For workflow related ID generation
 * 
 * @author gavinli
 *
 */
public final class LocalIdGenerator {

    public static final String KEY_MODULE = LocalIdGenerator.class.getSimpleName();
    public static final LocalIdGenerator INSTANCE = new LocalIdGenerator();

    private static final String BASE62_CHARS_STR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static char[] BASE62_CHARS;

    private static final long COUNT_MIN = 10000;
    private static final long COUNT_MAX = 90000;

    private AtomicLong count = new AtomicLong(COUNT_MIN);

    private Random r = new Random();

    private String idSaltStr;

    private LocalIdGenerator() {
        BASE62_CHARS = BASE62_CHARS_STR.toCharArray();
        int salt = new Random().nextInt(62);
        idSaltStr = String.valueOf(BASE62_CHARS[salt]);

        String moduleIdentity = System.getProperty(KEY_MODULE);
        if (moduleIdentity != null && moduleIdentity.trim().length() > 0) {
            idSaltStr += moduleIdentity.trim();
        }
    }

    public static String uuid() {
        String s = UUID.randomUUID().toString();
        return s.replace("-", "");
    }

    public static String generateId() {
        return INSTANCE.doGenerateTimestampedId();
    }

    public static String generateId(String prefix) {
        return INSTANCE.generateTimestampedId(prefix);
    }

    public String generateTimestampedId(String prefix) {
        return prefix + generateTimestampedId();
    }

    public String generateTimestampedId() {
        return doGenerateTimestampedId();
    }

    private String doGenerateTimestampedId() {
        String timeStr = number2base62(System.currentTimeMillis());
        String countStr = doGenerateCountStr();

        int salt = r.nextInt(62);
        String saltStr = String.valueOf(BASE62_CHARS[salt]);

        return timeStr + idSaltStr + countStr + saltStr;
    }

    private synchronized String doGenerateCountStr() {
        if (count.get() >= COUNT_MAX) {
            count.set(COUNT_MIN);
        }

        return number2base62(count.incrementAndGet());
    }

    private String number2base62(long num) {

        StringBuilder sb = new StringBuilder();
        long mod = num % 62;
        sb.append(BASE62_CHARS[(int) mod]);

        if (num >= 62) {
            num = num / 62;
            while (true) {
                if (num < 62) {
                    sb.append(BASE62_CHARS[(int) num]);
                    break;
                }

                mod = num % 62;
                sb.append(BASE62_CHARS[(int) mod]);

                num = num / 62;
            }
        }

        return sb.reverse().toString();
    }
}
