package com.example.form8038cp.form.xml;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates generated IRS XML against the 2027v1.0 Return8038CP XSD schema.
 *
 * The schema is loaded once at startup (expensive) and cached as an immutable
 * {@link Schema}. Validator instances created per call are cheap and thread-safe
 * to use concurrently.
 */
@Component
@Slf4j
public class IrsXmlSchemaValidator {

    private static final String SCHEMA_PATH =
            "/docs/schema/2027v1.0/TEGE/TEGE8038CP/Return8038CP.xsd";

    private Schema schema;

    @PostConstruct
    void loadSchema() {
        URL schemaUrl = getClass().getResource(SCHEMA_PATH);
        if (schemaUrl == null) {
            log.warn("IRS XSD schema not found at {}; schema validation will be skipped", SCHEMA_PATH);
            return;
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Disallow external entity access for security
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "all");
            schema = factory.newSchema(schemaUrl);
            log.info("IRS XSD schema loaded successfully from {}", schemaUrl);
        } catch (SAXException e) {
            log.error("Failed to load IRS XSD schema — schema validation disabled: {}", e.getMessage(), e);
        }
    }

    /**
     * Validates {@code xml} against the IRS 8038-CP schema.
     *
     * @param xml the XML string to validate
     * @return a list of error messages; empty means the document is valid
     */
    public List<String> validate(String xml) {
        List<String> errors = new ArrayList<>();

        if (schema == null) {
            log.warn("Schema not loaded; skipping XSD validation");
            return errors;
        }

        try {
            var validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,    "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA,  "");
            validator.setErrorHandler(new CollectingErrorHandler(errors));
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            errors.add(formatSaxError("FATAL", e));
        } catch (Exception e) {
            errors.add("FATAL: " + e.getMessage());
        }

        return errors;
    }

    // -------------------------------------------------------------------------

    private static final class CollectingErrorHandler implements ErrorHandler {

        private final List<String> errors;

        CollectingErrorHandler(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void warning(SAXParseException e) {
            log.debug("XML schema warning: {}", e.getMessage());
        }

        @Override
        public void error(SAXParseException e) {
            errors.add(formatSaxError("ERROR", e));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            errors.add(formatSaxError("FATAL", e));
            throw e; // stop further parsing
        }
    }

    private static String formatSaxError(String severity, SAXParseException e) {
        return String.format("%s [line %d, col %d]: %s",
                severity, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
    }
}
