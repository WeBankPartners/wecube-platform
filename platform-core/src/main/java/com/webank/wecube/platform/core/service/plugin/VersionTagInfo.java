package com.webank.wecube.platform.core.service.plugin;

import java.util.regex.Matcher;

public class VersionTagInfo {
    public static final String VERSION_TAG_PATTERN = "^(#@((v|V)(\\d+)((\\.\\d+)*))-(begin|Begin|BEGIN|end|End|END)@)(.*)$";
    private static final String BORDER_INDICATOR_BEGIN = "begin";
    private static final String BORDER_INDICATOR_END = "end";
    private String rawVersionTag;
    private String version;
    private String borderIndicator;
    private Long lineNum;
    private VersionTagInfo counterParty;

    public static VersionTagInfo parseVersionTagInfo(Matcher m, Long num) {
        VersionTagInfo info = new VersionTagInfo();
        info.setLineNum(num);
        info.setRawVersionTag(m.group(0));
        info.setBorderIndicator(m.group(7));
        info.setVersion(m.group(2));

        return info;
    }

    public String getRawVersionTag() {
        return rawVersionTag;
    }

    public void setRawVersionTag(String rawVersionTag) {
        this.rawVersionTag = rawVersionTag;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBorderIndicator() {
        return borderIndicator;
    }

    public void setBorderIndicator(String borderIndicator) {
        this.borderIndicator = borderIndicator;
    }

    public Long getLineNum() {
        return lineNum;
    }

    public void setLineNum(Long lineNum) {
        this.lineNum = lineNum;
    }

    public VersionTagInfo getCounterParty() {
        return counterParty;
    }

    public void setCounterParty(VersionTagInfo cp) {
        if (cp == null) {
            return;
        }
        this.counterParty = cp;

        if (cp.getCounterParty() == null) {
            cp.setCounterParty(this);
        }
    }

    public boolean isBegin() {
        return BORDER_INDICATOR_BEGIN.equalsIgnoreCase(borderIndicator);
    }

    public boolean isEnd() {
        return BORDER_INDICATOR_END.equalsIgnoreCase(borderIndicator);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionTagInfo [rawVersionTag=");
        builder.append(rawVersionTag);
        builder.append(", version=");
        builder.append(version);
        builder.append(", borderIndicator=");
        builder.append(borderIndicator);
        builder.append(", lineNum=");
        builder.append(lineNum);
        builder.append("]");
        return builder.toString();
    }
}
