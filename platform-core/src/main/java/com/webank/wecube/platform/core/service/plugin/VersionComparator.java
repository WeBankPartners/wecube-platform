package com.webank.wecube.platform.core.service.plugin;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {
    private static final String VERSION_PREFIX = "v";

    @Override
    public int compare(String v1, String v2) {

        int[] v1Nums = tidyVersion(v1);
        int[] v2Nums = tidyVersion(v2);

        int compare = 0;
        for (int i = 0; i < v1Nums.length; i++) {
            compare = (v1Nums[i] - v2Nums[i]);
            if (compare != 0) {
                break;
            }
        }

        return compare;
    }

    private int[] tidyVersion(String versionStr) {
        if (isBlank(versionStr)) {
            throw new IllegalArgumentException();
        }

        if (VERSION_PREFIX.equalsIgnoreCase(versionStr.substring(0, 1))) {
            versionStr = versionStr.substring(1);
        }

        String[] vStrNums = versionStr.split("\\.");
        int[] vNums = new int[] { 0, 0, 0, 0 };
        for (int i = 0; i < vStrNums.length && i < vNums.length; i++) {
            vNums[i] = Integer.parseInt(vStrNums[i]);
        }

        return vNums;
    }

    private boolean isBlank(String s) {
        if ((s == null) || (s.trim().length() < 1)) {
            return true;
        }

        return false;
    }
}
