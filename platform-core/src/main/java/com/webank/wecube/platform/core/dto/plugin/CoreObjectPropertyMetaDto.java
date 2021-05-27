package com.webank.wecube.platform.core.dto.plugin;

public class CoreObjectPropertyMetaDto {
    
    public static final String SENSITIVE_YES = "Y";
    public static final String SENSITIVE_NO = "N";

    private String id;

    private String name;

    private String dataType;

    private String refType;

    private String refName;

    private String mapType;

    private String mapExpr;

    private String objectMetaId;

    private String objectName;

    private String packageName;

    private String source;

    private String sensitive;

    private CoreObjectMetaDto refObjectMeta;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String getMapExpr() {
        return mapExpr;
    }

    public void setMapExpr(String mapExpr) {
        this.mapExpr = mapExpr;
    }

    public String getObjectMetaId() {
        return objectMetaId;
    }

    public void setObjectMetaId(String objectMetaId) {
        this.objectMetaId = objectMetaId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSensitive() {
        return sensitive;
    }

    public void setSensitive(String sensitive) {
        this.sensitive = sensitive;
    }

    public CoreObjectMetaDto getRefObjectMeta() {
        return refObjectMeta;
    }

    public void setRefObjectMeta(CoreObjectMetaDto refObjectMeta) {
        this.refObjectMeta = refObjectMeta;
    }

}
