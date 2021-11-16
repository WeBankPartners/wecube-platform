

package com.webank.wecube.platform.core.service.plugin.xml.register;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileType", propOrder = {
    "source","toFile"
})
public class FileType {

    @XmlAttribute(name = "source", required = true)
    protected String source;
    @XmlAttribute(name = "toFile", required = true)
    protected String toFile;
    
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getToFile() {
        return toFile;
    }
    public void setToFile(String toFile) {
        this.toFile = toFile;
    }

   

}
