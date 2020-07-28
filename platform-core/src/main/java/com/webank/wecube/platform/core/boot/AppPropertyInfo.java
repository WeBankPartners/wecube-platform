package com.webank.wecube.platform.core.boot;

public class AppPropertyInfo {
    public static final String TABLE_NAME = "core_app_property";
    public static final int REV_INIT = 1;
    protected String name;
    protected String val;
    protected Integer rev;

    public AppPropertyInfo() {
        super();
    }

    public AppPropertyInfo(String name, String val, Integer rev) {
        super();
        this.name = name;
        this.val = val;
        this.rev = rev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public int getNextRev() {
        return this.getRev() + 1;
    }
    
    public Object[] unpack(){
        return new Object[]{this.getName(), this.getVal(), this.getRev()};
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AppPropertyInfo [name=");
        builder.append(name);
        builder.append(", val=");
        builder.append(val);
        builder.append(", rev=");
        builder.append(rev);
        builder.append("]");
        return builder.toString();
    }

}