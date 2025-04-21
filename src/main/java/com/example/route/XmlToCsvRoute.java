package com.example.route;

import com.example.route.XmlToCsvProcessor;            // Handles XML-to-CSV transformation
import com.example.route.ExceptionProcessor;           // Logs failed messages and stops route
import com.example.service.MessageCounter;             // Tracks received and written message counts
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XmlToCsvRoute extends RouteBuilder {

    // Inject the singleton MessageCounter to track processing statistics
    @Autowired
    private MessageCounter counter;

    @Override
    public void configure() throws Exception {

        // Set up a basic error handler with retry logic
        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(3)
            .redeliveryDelay(2000));

        // Main route: listen for JMS messages and delegate based on message content
        from("jms:queue:inputQueue")
            .routeId("xmlToCsvRoute")
            .transacted()  // Ensure JMS message is processed transactionally
            .choice()
                .when(xpath("/message/type = 'EOF'"))   // EOF message detected
                    .to("direct:handleEOF")
                .otherwise()                            // Regular XML message
                    .to("direct:transformAndAppend")
            .end();

        // Route to transform and append messages
        from("direct:transformAndAppend")
            .routeId("transformRoute")
            .doTry()
                // Count every received XML message
                .process(exchange -> counter.incrementReceived())

                // Perform transformation to CSV
                .process(new XmlToCsvProcessor())

                // Count every successfully transformed (and thus written) message
                .process(exchange -> counter.incrementWritten())

                // Append the resulting CSV row to working.csv
                .to("file://working?fileName=working.csv&fileExist=Append")

            // If anything fails in the above steps...
            .doCatch(Exception.class)
                // Log to JSON file and stop the route
                .process(new ExceptionProcessor())
                .log("ERROR: Message processing failed. Logged and aborted.")
                .stop()
            .end();

        // Route triggered by EOF message to finalize the batch
        from("direct:handleEOF")
            .routeId("eofRoute")
            .log("EOF Received - rotating file")
            .to("bean:fileRotator?method=rotateFile"); // This will also validate counts
    }
}