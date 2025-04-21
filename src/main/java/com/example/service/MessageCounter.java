package com.example.service;

import org.springframework.stereotype.Component;

@Component
public class MessageCounter {
    private int received = 0;
    private int written = 0;

    public synchronized void incrementReceived() {
        received++;
    }

    public synchronized void incrementWritten() {
        written++;
    }

    public synchronized int getReceived() {
        return received;
    }

    public synchronized int getWritten() {
        return written;
    }

    public void reset() {
        received = 0;
        written = 0;
    }
}