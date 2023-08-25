package net.exemine.api.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogUtilTest {

    private final Logger logger = Logger.getLogger(LogUtil.class.getName());
    private TestHandler testHandler;

    @BeforeEach
    public void setUpStreams() {
        testHandler = new TestHandler();
        logger.addHandler(testHandler);
        logger.setLevel(Level.ALL);
    }

    @AfterEach
    public void restoreStreams() {
        logger.removeHandler(testHandler);
    }

    @Test
    public void testInfo() {
        LogUtil.info("Test info message");
        assertTrue(testHandler.getMessage().contains("Test info message"));
    }

    @Test
    public void testWarning() {
        LogUtil.warning("Test warning message");
        assertTrue(testHandler.getMessage().contains("Test warning message"));
    }

    @Test
    public void testError() {
        LogUtil.error("Test error message");
        assertTrue(testHandler.getMessage().contains("Test error message"));
    }

    private static class TestHandler extends Handler {

        private String message;

        @Override
        public void publish(LogRecord record) {
            message = record.getMessage();
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}

        public String getMessage() {
            return message;
        }
    }
}