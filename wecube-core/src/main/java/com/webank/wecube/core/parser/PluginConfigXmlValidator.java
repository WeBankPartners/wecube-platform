package com.webank.wecube.core.parser;

import com.webank.wecube.core.commons.WecubeCoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;

public class PluginConfigXmlValidator {
    private Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(PluginConfigXmlValidator.class);

    public PluginConfigXmlValidator() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        URL resource = new ClassPathResource("/plugin/plugin-config-v2.xsd").getURL();
        Schema schema = schemaFactory.newSchema(resource);
        validator = schema.newValidator();
    }

    public void validate(StreamSource xmlSource) throws WecubeCoreException {
        // xml source is the xml file path
        try {
            validator.validate(xmlSource);
        } catch (SAXException | IOException ex) {
            if (logger.isDebugEnabled()) logger.debug(xmlSource.toString());
            throw new WecubeCoreException("XML validate failed: "+ex.getMessage());
        }
    }
}
