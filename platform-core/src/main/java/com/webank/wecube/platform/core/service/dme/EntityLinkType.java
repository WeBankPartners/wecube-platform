package com.webank.wecube.platform.core.service.dme;

public enum EntityLinkType {

    REF_TO(">"), REF_BY("~");

    private String symbol;

    private EntityLinkType(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static EntityLinkType entityLinkType(String symbol) {
        if (isRefTo(symbol)) {
            return REF_TO;
        }

        if (isRefBy(symbol)) {
            return REF_BY;
        }

        return null;
    }

    public static boolean isRefTo(String symbol) {
        if (symbol == null || symbol.trim().length() <= 0) {
            return false;
        }

        return REF_TO.symbol().equals(symbol);
    }

    public static  boolean isRefBy(String symbol) {
        if (symbol == null || symbol.trim().length() <= 0) {
            return false;
        }

        return REF_BY.symbol().equals(symbol);
    }

}
