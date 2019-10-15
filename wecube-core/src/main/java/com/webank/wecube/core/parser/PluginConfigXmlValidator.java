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
            throw new WecubeCoreException("XML validation failed: " + ex.getMessage());
        }
    }

    public void validate(InputStream inputStream) throws WecubeCoreException {
        validate(new StreamSource(inputStream));
    }

    public void validate(String definitionXmlFullName) throws WecubeCoreException {
        InputStream inputStream = null;
        try {
            inputStream = new ClassPathResource(definitionXmlFullName).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        validate(inputStream);
    }
}
