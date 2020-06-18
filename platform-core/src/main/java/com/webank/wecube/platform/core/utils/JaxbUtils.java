package com.webank.wecube.platform.core.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class JaxbUtils {
    public static final String DEFAULT_ENCODING = "UTF-8";

    @SuppressWarnings("restriction")
    public static String convertToXml(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null.");
        }
        String result = null;
        StringWriter writer = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, DEFAULT_ENCODING);
            // marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            marshaller.setProperty("com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler",
                    new com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler() {
                        public void escape(char[] chars, int start, int length, boolean isAttVal, Writer writer)
                                throws IOException {
                            writer.write(chars, start, length);
                        }
                    });

            marshaller.marshal(obj, writer);
            result = writer.toString().replace("standalone=\"yes\"", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException("Errors while closing.");
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertToObject(String xml, Class<T> clazz) {
        T result = null;
        StringReader reader = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            reader = new StringReader(xml);
            result = (T) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return result;
    }

    public static void createXSD(Class<?>[] classes, String dir, String fileName) {
        try {
            SchemaOutputResolver resolver = new CustomSchemaOutputResolver(dir, fileName);
            JAXBContext context = JAXBContext.newInstance(classes);
            context.generateSchema(resolver);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CustomSchemaOutputResolver extends SchemaOutputResolver {
        private File file;

        public CustomSchemaOutputResolver(String dir, String fileName) {
            super();

            try {
                file = new File(dir, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(file);
        }

    }
}
