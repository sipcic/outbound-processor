package com.example.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ExceptionProcessor implements Processor {

    private static final String EXCEPTION_DIR = "exception"; // Make sure this folder exists

    @Override
    public void process(Exchange exchange) throws Exception {
        // Extract the exception and message details
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String messageBody = exchange.getIn().getBody(String.class);
        String messageId = exchange.getExchangeId();

        // Prepare JSON content
        Map<String, Object> error = new HashMap<>();
        error.put("messageId", messageId);
        error.put("messageBody", messageBody);
        error.put("stackTrace", getStackTrace(exception));

        // Create exception directory if it doesn't exist
        File dir = new File(EXCEPTION_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Write to JSON file named with messageId
        File outFile = new File(dir, messageId + ".json");
        try (FileWriter writer = new FileWriter(outFile)) {
            new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValue(writer, error);
        }
    }

    private String getStackTrace(Exception e) {
        if (e == null) return "No exception caught";
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            sb.append(elem.toString()).append("\n");
        }
        return sb.toString();
    }
}