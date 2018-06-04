package com.ubtrobot.speech;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ubtrobot.speech.understand.SAXParseHelper;
import com.ubtrobot.speech.understand.XmlParseHelper;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.logger.android.AndroidLoggerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class XmlTest {
    @Before
    public void setLog() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        FwLoggerFactory.setup(new AndroidLoggerFactory());
    }

    @Test
    public void testXml() {
        XmlParseHelper xmlParseHelper = new XmlParseHelper();
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            InputStream slots = appContext
                    .getAssets().open("slots.xml");
            xmlParseHelper.setInputSource(slots);
            xmlParseHelper.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaxXml() {
        SAXParseHelper xmlParseHelper = new SAXParseHelper();
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            InputStream slots = appContext
                    .getAssets().open("slots.xml");
            xmlParseHelper.parse(slots);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
