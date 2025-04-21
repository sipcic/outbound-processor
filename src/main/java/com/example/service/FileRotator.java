package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileRotator is responsible for:
 * - Finalizing the current working file when an EOF is received
 * - Renaming it with a timestamp
 * - Moving it to the output directory
 * - Resetting the working file for the next batch
 * - Validating that message counts match
 */
@Component
public class FileRotator {

    private static final String WORKING_FILE_PATH = "working/working.csv";
    private static final String OUTPUT_DIR = "output";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private MessageCounter counter;

    public void rotateFile() {
        try {
            Path workingFile = Paths.get(WORKING_FILE_PATH);
            if (!Files.exists(workingFile)) {
                System.out.println("No working file to rotate.");
                return;
            }

            // Generate output filename with timestamp
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String outputFileName = "output_" + timestamp + ".csv";
            Path outputFile = Paths.get(OUTPUT_DIR, outputFileName);

            // Ensure output directory exists
            Files.createDirectories(Paths.get(OUTPUT_DIR));

            // Move and rename the working file
            Files.move(workingFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Rotated file: " + outputFile);

            // ✅ Validation: Compare message counts
            int received = counter.getReceived();
            int written = counter.getWritten();

            System.out.println("Received XML Messages: " + received);
            System.out.println("Written CSV Records: " + written);
            if (received == written) {
                System.out.println("Validation: ✅ PASS");
            } else {
                System.out.println("Validation: ❌ FAIL");
            }

            // Reset message counters for the next batch
            counter.reset();

        } catch (IOException e) {
            throw new RuntimeException("Failed to rotate file", e);
        }
    }
}