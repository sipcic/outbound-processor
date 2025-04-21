package com.example.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

public class XmlToCsvProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String xml = exchange.getIn().getBody(String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        String id = doc.getElementsByTagName("id").item(0).getTextContent();
        String name = doc.getElementsByTagName("name").item(0).getTextContent();
        String value = doc.getElementsByTagName("value").item(0).getTextContent();

        String csvRow = String.format("%s,%s,%s\n", id, name, value);
        exchange.getIn().setBody(csvRow);
    }
}
