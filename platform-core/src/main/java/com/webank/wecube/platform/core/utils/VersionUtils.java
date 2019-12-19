package com.webank.wecube.platform.core.utils;

import org.apache.commons.lang3.math.NumberUtils;

public class VersionUtils {
    private static final String VERSION_SEPARATORS = "[./-]";

    public static boolean isLeftVersionGreatThanRightVersion(String leftVersion, String rightVersion) {
        return compare(leftVersion, rightVersion) > 0;
    }

    public static int compare(String leftVersion, String rightVersion) {
        if (leftVersion == null || rightVersion == null) return 0;
        if (leftVersion.equals(rightVersion)) return 0;
        leftVersion = ignoreCharacterV(leftVersion);
        rightVersion = ignoreCharacterV(rightVersion);
        String[] segmentsOfLeftVersion = separate(leftVersion);
        String[] segmentsOfRightVersion = separate(rightVersion);
        for (int i=0; i < segmentsOfLeftVersion.length && i <segmentsOfRightVersion.length; i++) {
            String segmentOfLeftVersion = segmentsOfLeftVersion[i];
            String segmentOfRightVersion = segmentsOfRightVersion[i];

            if (segmentOfLeftVersion.equals(segmentOfRightVersion)) continue;

            try {
                int x = Integer.parseInt(segmentOfLeftVersion);
                int y = Integer.parseInt(segmentOfRightVersion);
                if (x == y) continue;
                return NumberUtils.compare(x, y);
            } catch (NumberFormatException exception) {
                return segmentOfLeftVersion.compareTo(segmentOfRightVersion);
            }
        }
        return NumberUtils.compare(segmentsOfLeftVersion.length, segmentsOfRightVersion.length);
    }

    private static String ignoreCharacterV(String version) {
        if (version.startsWith("v") || version.startsWith("V")) return version.substring(1);
        return version;
    }

    private static String[] separate(String version) {
        return version.split(VERSION_SEPARATORS);
    }


}
