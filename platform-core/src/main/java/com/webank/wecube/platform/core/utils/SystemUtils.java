package com.webank.wecube.platform.core.utils;

public class SystemUtils {

    private static String TEMP_FOLDER_PATH = null;

    public static String getTempFolderPath() {
        if (TEMP_FOLDER_PATH == null) {
            TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir");
            if (!TEMP_FOLDER_PATH.endsWith("/") && !TEMP_FOLDER_PATH.endsWith("\\")) {
                TEMP_FOLDER_PATH = TEMP_FOLDER_PATH + "/";
            }
        }
        return TEMP_FOLDER_PATH;
    }
}
