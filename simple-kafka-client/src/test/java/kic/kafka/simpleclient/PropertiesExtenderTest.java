package kic.kafka.simpleclient;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PropertiesExtenderTest {

    @Test
    public void testExtend() throws Exception {
        Properties p = new Properties();
        p.setProperty("test-a", "A");
        p.setProperty("test-b", "B");

        Properties pe = new PropertiesExtender(p).with("test-b", "b")
                                                 .extend();

        Assert.assertEquals(p.getProperty("test-a"), pe.getProperty("test-a"));
        Assert.assertEquals("b", pe.getProperty("test-b"));
        Assert.assertNotEquals(p.getProperty("test-b"), pe.getProperty("test-b"));
    }

}