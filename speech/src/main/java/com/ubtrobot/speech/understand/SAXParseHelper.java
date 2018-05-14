package com.ubtrobot.speech.understand;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.validate.Preconditions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SAXParseHelper {

    private Logger LOGGER = FwLoggerFactory.getLogger("SAXParseHelper");

    private static final String TAG_SOURCE_LIST = "source-list";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_NAME = "name";

    SAXParserFactory factory = null;
    SAXParser parser;
    private StringBuilder sb =new StringBuilder();
    private List<String> sources = new LinkedList<>();
    private HashMap<String, Intent> mapper = new HashMap<>();
    private LinkedList<String> tagRecoder = new LinkedList<>();

    public SAXParseHelper() {
        factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            LOGGER.e("Create SAXParser instance exception.");
            e.printStackTrace();
        } catch (SAXException e) {
            LOGGER.e("Create SAXParser instance exception.");
            e.printStackTrace();
        }

    }

    public void parse(InputStream is) {
        try {
            parser.parse(is, mHandler);
        } catch (SAXException e) {
            LOGGER.e("SAXParser parse exception.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DefaultHandler mHandler = new DefaultHandler() {
        @Override
        public void startDocument() throws SAXException {
            LOGGER.i("startDocument");
        }

        @Override
        public void endDocument() throws SAXException {
            LOGGER.i("endDocument");

        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            LOGGER.i("startElement:" + localName);
            tagRecoder.offerLast(localName);
            if (TAG_SOURCE_LIST.equals(localName)) {

            } else if (TAG_SOURCE.equals(localName)) {
            }
            sb.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            LOGGER.i("endElement:" + localName);
            String text = sb.toString();
            String tagName = tagRecoder.pollLast();
            if (TAG_SOURCE_LIST.equals(tagName)) {

            } else if (TAG_SOURCE.equals(tagName)) {
                Preconditions.checkStringNotEmpty(text,
                        "<source>must not be empty here</source>");
                LOGGER.i("add source:" + sb.toString());
                sources.add(text);
                mapper.put(text, new Intent());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            LOGGER.i("characters:" + new String(ch, start, length).trim());
            sb.append(new String(ch, start, length));
        }
    };
}
