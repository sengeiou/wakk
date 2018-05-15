package com.ubtrobot.speech.understand;

import android.text.TextUtils;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.validate.Preconditions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XmlParseHelper {

    private Logger LOGGER = FwLoggerFactory.getLogger("XmlParseHelper");

    private static final String TAG_SOURCE_LIST = "source-list";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_UNDERSTAND_RESULT = "understand-result";

    private static final String TAG_INTENT = "intent";
    private static final String TAG_NAME = "name";
    private static final String TAG_ACTION = "action";
    private static final String TAG_PARAMETERS = "parameter";

    private static final String TAG_FULLFILLMENT_MESSAGES_PAYLOAD = "fullfillment-messages-payload";
    private static final String TAG_FIELD_NAME = "field-name";

    XmlPullParserFactory factory = null;
    XmlPullParser parser;

    private List<String> sources = new LinkedList<>();
    private Map<String, Map<String, Intent>> mapper = new HashMap<>();
    private LinkedList<String> tagRecoder = new LinkedList<>();

    private Map<String, String> intentKey = new HashMap<>();

    public XmlParseHelper() {
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            LOGGER.e("Create XmlPullParserFactory or XmlPullParser instance exception.");
            e.printStackTrace();
        }
    }

    public void setInputSource(String path) {
        try {
            setInput(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, Intent>> getMapper() {
        return mapper;
    }

    public void setInputSource(InputStream inputStream) {
        setInput(inputStream);
    }

    private void setInput(InputStream input) {
        try {
            parser.setInput(input, "utf-8");
        } catch (XmlPullParserException e) {
            LOGGER.e("parser setInput exception.");
            e.printStackTrace();
        }
    }

    public void parse() {
        try {
            int eventType = parser.getEventType();
            LOGGER.i("parase :" + eventType);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tagName = parser.getName();
                        if (TAG_SOURCE.equals(tagName)) {
                            parseSource(parser, tagName);
                            eventType = parser.next();
                            continue;
                        }

                        if (TAG_UNDERSTAND_RESULT.equals(tagName)) {
                            String intentName = parser.getAttributeValue(null, "intent-name");
                            Preconditions.checkStringNotEmpty(intentName,
                                    "understand-result attribute \"intent-name\" refuse null or "
                                            + "missed");
                            parser.nextTag();
                            parseIntent(parser, parser.getName(), intentName);
                            eventType = parser.nextTag();
                            parserFulfillment(parser, parser.getName());
                            continue;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            LOGGER.i("parse success!!!");
        } catch (XmlPullParserException e) {
            LOGGER.i("parse xml error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseSource(XmlPullParser parser, String endTag)
            throws IOException, XmlPullParserException {
        int eventType;
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {

            String text = parser.getText();
            Preconditions.checkStringNotEmpty(text,
                    "<source>must not be empty here</source>");
            LOGGER.i("add source:" + text.toString());
            sources.add(text);
            mapper.put(text, new HashMap<String, Intent>());
        }
    }

    private void parseIntent(XmlPullParser parser, String endTag, String intentName)
            throws IOException, XmlPullParserException {
        int eventType;
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (TAG_NAME.equals(tagName)) {
                        //todo 需保证这里只会进来一次
                        List<String> allSource = new LinkedList<>(sources);
                        parseIntentName(parser, tagName, intentName, allSource);
                        fillDefaultName(allSource, intentName);
                        continue;
                    }
                    if (TAG_ACTION.equals(tagName)) {
                        String defaultAction = parser.getAttributeValue(null, "value");
                        Preconditions.checkStringNotEmpty(defaultAction,
                                "Action attribute \"value\" refuse null or missed");

                        List<String> allSource = new LinkedList<>(sources);
                        parseIntentAction(parser, tagName, allSource);
                        fillDefaultAction(allSource, defaultAction);
                        continue;
                    }
                    if (TAG_PARAMETERS.equals(tagName)) {
                        String nameValue = parser.getAttributeValue(null, "value");
                        Preconditions.checkStringNotEmpty(nameValue,
                                "Parameters attribute \"value\" refuse null or missed");
                        String typeValue = parser.getAttributeValue(null, "type");
                        if (TextUtils.isEmpty(typeValue)) {
                            typeValue = "single";
                        }

                        List<String> allSource = new LinkedList<>(sources);
                        parseIntentParameters(parser, parser.getName(), allSource);
                        Intent.Param param = new Intent.Param();
                        param.setType(typeValue);
                        param.setValue(nameValue);
                        fillDefaultParameter(allSource, param);
                        continue;
                    }
            }
        }
    }


    private void fillDefaultName(List<String> defaultsSource, String value) {
        for (String source : defaultsSource) {
            Map<String, Intent> intentMap = mapper.get(source);
            Intent intent = new Intent();
            intent.setDestName(value);
            intentMap.put(value, intent);
            //补充没记录的source
            intentKey.put(source, value);
        }
    }

    private void fillDefaultAction(List<String> defaultsSource, String value) {
        for (String source : defaultsSource) {
            Map<String, Intent> intentMap = mapper.get(source);
            String key = intentKey.get(source);
            Intent intent = getOrCreateIntent(intentMap, key);
            intent.setDestAction(value);
        }
    }

    private void fillDefaultParameter(List<String> defaultsSource, Intent.Param param) {
        for (String source : defaultsSource) {
            Map<String, Intent> intentMap = mapper.get(source);
            String key = intentKey.get(source);
            Intent intent = getOrCreateIntent(intentMap, key);
            intent.getIntentParameterMap().put(param.getValue(), param);
        }
    }

    private Intent getOrCreateIntent(Map<String, Intent> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            return map.get(key);
        }
        Intent intent = new Intent();
        map.put(key, intent);
        return intent;
    }

    private void parseIntentName(XmlPullParser parser, String endTag, String intentName,
            List<String> allSource)
            throws IOException, XmlPullParserException {
        int eventType;
        intentKey.clear();
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    checkSourceName(tagName);
                    allSource.remove(tagName);
                    Map<String, Intent> intentMap = mapper.get(tagName);
                    String text = parser.nextText();
                    Intent intent = getOrCreateIntent(intentMap, text);
                    intent.setDestName(intentName);
                    intentKey.put(tagName, text);
                    break;
                default:
                    break;
            }
        }
    }

    private void parseIntentAction(XmlPullParser parser, String endTag, List<String> allSource)
            throws IOException, XmlPullParserException {
        int eventType;
        String value = parser.getAttributeValue(null, "value");
        Preconditions.checkStringNotEmpty(value,
                "Action attribute \"value\" refuse null or missed");
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    checkSourceName(tagName);
                    allSource.remove(tagName);
                    Map<String, Intent> intentMap = mapper.get(tagName);
                    String text = parser.nextText();
                    String key = intentKey.get(tagName);
                    Intent intent = getOrCreateIntent(intentMap, key);
                    intent.setDestAction(value);
                    break;
                default:
                    break;
            }
        }
    }

    private void parseIntentParameters(XmlPullParser parser, String endTag, List<String> allSource)
            throws IOException, XmlPullParserException {
        int eventType;
        String nameValue = parser.getAttributeValue(null, "value");
        //todo 这里需要参数检测null
        Preconditions.checkStringNotEmpty(nameValue,
                "Parameters attribute \"value\" refuse null or missed");
        String typeValue = parser.getAttributeValue(null, "type");
        if (TextUtils.isEmpty(typeValue)) {
            typeValue = "single";
        }
        //todo 这里需要参数检查null
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    checkSourceName(tagName);
                    allSource.remove(tagName);
                    Map<String, Intent> intentMap = mapper.get(tagName);
                    String text = parser.nextText();
                    String key = intentKey.get(tagName);
                    Intent intent = getOrCreateIntent(intentMap, key);
                    Intent.Param param = new Intent.Param();
                    param.setValue(nameValue);
                    param.setType(typeValue);
                    intent.getIntentParameterMap().put(text, param);
                    break;
                default:
                    break;
            }
        }
    }

    private void parserFulfillment(XmlPullParser parser, String endTag)
            throws IOException, XmlPullParserException {
        int eventType;
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (TAG_FIELD_NAME.equals(tagName)) {
                        List<String> allSource = new LinkedList<>(sources);
                        String value = parser.getAttributeValue(null, "value");
                        Preconditions.checkStringNotEmpty(value,
                                "Parameters attribute \"value\" refuse null or missed");
                        parserFieldName(parser, tagName, allSource);
                        fillDefaultFulfillment(allSource, value);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void fillDefaultFulfillment(List<String> defaultsSource, String value) {
        for (String source : defaultsSource) {
            Map<String, Intent> intentMap = mapper.get(source);
            String key = intentKey.get(source);
            Intent intent = getOrCreateIntent(intentMap, key);
            intent.getFulfillment().put(value, value);
        }
    }

    private void parserFieldName(XmlPullParser parser, String endTag, List<String> allSource)
            throws IOException, XmlPullParserException {
        int eventType;
        String value = parser.getAttributeValue(null, "value");
        Preconditions.checkStringNotEmpty(value,
                "Parameters attribute \"value\" refuse null or missed");
        while (XmlPullParser.END_TAG != (eventType = parser.next()) ||
                !endTag.equals(parser.getName())) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    checkSourceName(tagName);
                    allSource.remove(tagName);
                    Map<String, Intent> intentMap = mapper.get(tagName);
                    String text = parser.nextText();
                    String key = intentKey.get(tagName);
                    Intent intent = getOrCreateIntent(intentMap, key);
                    intent.getFulfillment().put(text, value);
                    break;
                default:
                    break;
            }
        }
    }

    private void checkSourceName(String name) {

        if (null == sources || sources.size() == 0) {
            throw new IllegalArgumentException(" make sure\n"
                    + "    <source-list>\n"
                    + "        <source>aaa</source>\n"
                    + "        <source>bbb</source>\n"
                    + "        <source>ccc</source>\n"
                    + "    </source-list> contain at least one <source> tag");
        }

        if (!sources.contains(name)) {
            throw new IllegalStateException(
                    " make sure tag name (" + name + ") is in the\n"
                            + "    <source-list>\n"
                            + "        <source>aaa</source>\n"
                            + "        <source>bbb</source>\n"
                            + "        <source>ccc</source>\n"
                            + "    </source-list>");
        }
    }
}
