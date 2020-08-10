package com.webank.wecube.platform.core.parser;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class PluginConfigXmlValidator {
    final String XML_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    final String XSD_PATH = "/plugin/plugin-config-v2.xsd";

    private Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(PluginConfigXmlValidator.class);

    public PluginConfigXmlValidator() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XML_NAMESPACE);
        URL resource = new ClassPathResource(XSD_PATH).getURL();
        Schema schema = schemaFactory.newSchema(resource);
        validator = schema.newValidator();
    }

    public void validate(StreamSource xmlSource) throws WecubeCoreException {
        // xml source is the xml file path
        try {
            validator.validate(xmlSource);
        } catch (SAXException | IOException ex) {
            if (logger.isDebugEnabled()) logger.debug(xmlSource.toString());
            throw new WecubeCoreException("3270",String.format("XML validation failed: %s" , ex.getMessage()));
        }
    }

    public void validate(InputStream inputStream) throws WecubeCoreException {
        validate(new StreamSource(inputStream));
    }

    public void validate(String definitionXmlFullName) throws WecubeCoreException {
        InputStream inputStream;
        try {
            inputStream = new ClassPathResource(definitionXmlFullName).getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage());
            if (logger.isDebugEnabled()) logger.debug(definitionXmlFullName);
            throw new WecubeCoreException(e.getMessage());
        }
        validate(inputStream);
    }
}
