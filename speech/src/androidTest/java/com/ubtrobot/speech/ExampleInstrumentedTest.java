package com.ubtrobot.speech;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ubtrobot.nlp.http.DTPackage;
import com.ubtrobot.nlp.http.EmotibotService;
import com.ubtrobot.nlp.http.NlpHttpService;
import com.ubtrobot.nlp.http.Param;
import com.ubtrobot.nlp.http.RetrofitWrapper;
import com.ubtrobot.retrofit.adapter.urest.URestCall;
import com.ubtrobot.speech.understand.AbstractConverter;
import com.ubtrobot.speech.understand.EmotibotConverter;
import com.ubtrobot.speech.understand.UnderstandResult;
import com.ubtrobot.speech.understand.XmlParseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.ubtrobot.speech.test", appContext.getPackageName());
    }

    @Test
    public void jsonTest() throws Exception {
        String json = "{\n"
                + "                \"param\": {\n"
                + "                    \"entities_root>>city\": [\n"
                + "                        \"昆明\"\n"
                + "                    ]\n"
                + "                }\n"
                + "            }";

        JSONObject object = new JSONObject(json);
        Object param = object.optJSONObject("param").opt("entities_root>>city");
        Log.i("test", "value:" + (param instanceof JSONArray));

    }

    @Test
    public void nlpTestCsAirport() throws Exception {
        NlpHttpService nlpHttpService = RetrofitWrapper.get().create(NlpHttpService.class);
        Param param = new Param();
        param.setTransactionId("AB_001_09ad981b");
        param.setRequestTime(System.currentTimeMillis() + "");
        param.setUbtNlpAppId("20000102");
        param.setDeviceId("ubt_device");
        param.setInputValue("北京到深圳的航班");
        param.setInputType("1");
        param.setLen("");
        param.setSessionId("1111");
        param.setLocation("深圳市");
        param.setApiVersion("V_01");

        URestCall<DTPackage> understand = nlpHttpService.understand(param);
        DTPackage execute = understand.execute();
        Log.i("test", "response:" + execute.toString());
    }

    @Test
    public void nlpTestTsl() throws Exception {
        NlpHttpService nlpHttpService = RetrofitWrapper.get().create(NlpHttpService.class);
        Param param = new Param();
        param.setTransactionId("AB_001_09ad981b");
        param.setRequestTime(System.currentTimeMillis() + "");
        param.setUbtNlpAppId("20000103");
        param.setDeviceId("ubt_device");
        param.setInputValue("我想听刘德华的摇滚励志歌曲忘情水");
        param.setInputType("1");
        param.setLen("");
        param.setSessionId("1111");
        param.setLocation("深圳市");
        param.setApiVersion("V_01");

        URestCall<DTPackage> understand = nlpHttpService.understand(param);
        DTPackage execute = understand.execute();
        Log.i("test", "response:" + execute.toString());
    }

    @Test
    public void nlpTestEasyHome() throws Exception {
        NlpHttpService nlpHttpService = RetrofitWrapper.get().create(NlpHttpService.class);
        Param param = new Param();
        param.setTransactionId("AB_001_09ad981b");
        param.setRequestTime(System.currentTimeMillis() + "");
        param.setUbtNlpAppId("20000104");
        param.setDeviceId("ubt_device");
        param.setInputValue("居然之家在哪里");
        param.setInputType("1");
        param.setLen("");
        param.setSessionId("1111");
        param.setLocation("深圳市");
        param.setApiVersion("V_01");

        URestCall<DTPackage> understand = nlpHttpService.understand(param);
        DTPackage execute = understand.execute();
        Log.i("test", "response:" + execute.toString());
    }


    @Test
    public void nluResult() throws Exception {
        Gson gson = new Gson();
        UnderstandResult.Builder builder = new UnderstandResult.Builder();
        builder.setSessionId("dsdasd");
        builder.setLanguage("zh-CN");
        builder.setInputText("今天天气怎么样");
        builder.setSource("Google");
        builder.setActionIncomplete(true);

        List<UnderstandResult.Context> lists = new ArrayList<>();

        UnderstandResult.Context.Builder contextBuilder = new UnderstandResult.Context.Builder();
        contextBuilder.setName("weather");
        contextBuilder.setParameters(new JSONObject("{\n"
                + "          \"date\": \"2018-05-04\",\n"
                + "          \"date.original\": \"today\",\n"
                + "          \"geo-city\": \"Moscow\",\n"
                + "          \"geo-city.original\": \"Moscow\"\n"
                + "        }"));

        lists.add(contextBuilder.build());

        UnderstandResult.Context.Builder contextBuilder1 = new UnderstandResult.Context.Builder();
        contextBuilder1.setName("weather");
        contextBuilder1.setParameters(new JSONObject("{\n"
                + "          \"date\": \"2018-05-04\",\n"
                + "          \"date.original\": \"today\",\n"
                + "          \"geo-city\": \"Moscow\",\n"
                + "          \"geo-city.original\": \"Moscow\"\n"
                + "        }"));

        lists.add(contextBuilder1.build());

        builder.setContexts(lists);

        UnderstandResult.Intent.Builder intentBuilder1 = new UnderstandResult.Intent.Builder();
        intentBuilder1.setDisplayName("xxxx/xxx/xxx");
        intentBuilder1.setName("weather");
        intentBuilder1.setScore(0.8888f);
        intentBuilder1.setParameters(new JSONObject("{\n"
                + "          \"date\": \"2018-05-04\",\n"
                + "          \"geo-city\": \"Moscow\"\n"
                + "        }"));

        builder.setIntent(intentBuilder1.build());

        UnderstandResult.Fulfillment.Builder fullfillMentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        fullfillMentBuilder.setSpeech("find weather");

        List<UnderstandResult.Message> messageLists = new ArrayList<>();

        UnderstandResult.Message.Builder messageBuilder1 = new UnderstandResult.Message.Builder();
        messageBuilder1.setType("weather1");
        messageBuilder1.setPlatform("service1");
        messageBuilder1.setParameters(new JSONObject("{\n"
                + "                    \"songUrl\":\"http://xxxx.sss.com/abc.mp3\",\n"
                + "                    \"title\":\"忘情水\",\n"
                + "                    \"singer\":\"刘德华\",\n"
                + "                    \"picUrl\":\"http://xxxx.sss.com/abc.jpg\"\n"
                + "                }"));

        messageLists.add(messageBuilder1.build());

        UnderstandResult.Message.Builder messageBuilder2 = new UnderstandResult.Message.Builder();
        messageBuilder2.setType("text");
        messageBuilder2.setPlatform("service2");
        messageBuilder2.setParameters(new JSONObject("{\n"
                + "                    \"speech\":\"马上给你找天气数据\"\n"
                + "                }"));

        messageLists.add(messageBuilder2.build());


        fullfillMentBuilder.setMessages(messageLists);

        UnderstandResult.Status.Builder statusBuilder1 = new UnderstandResult.Status.Builder();

        statusBuilder1.setCode(206);
        statusBuilder1.setErrorDetails("partial_content");
        statusBuilder1.setErrorMessage("Webhook call failed. Error: Request timeout.");

        fullfillMentBuilder.setStatus(statusBuilder1.build());

        builder.setFulfillment(fullfillMentBuilder.build());

        String s = gson.toJson(builder.build());
        Log.i("test", "s:" + s);
    }

    @Test
    public void nluEmotibot() throws Exception {
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

        Gson gson = new Gson();
        EmotibotService nlpHttpService = RetrofitWrapper.get().createEmotibotService(
                EmotibotService.class);
        Call<JsonObject> understand = nlpHttpService.understand(
                "c8f64283751155611dfc7842aeaacca9",
                "b0f1ecccdc04",
                "带我去洗手间",
                "chat",
                "深圳市");
        Response<JsonObject> execute = understand.execute();
        String string = "";

        string = execute.body().toString();
        Log.i("test", "response:" + string);

        JSONObject root = new JSONObject(string);
        AbstractConverter converter = null;
        converter = new EmotibotConverter(root,
                xmlParseHelper.getMapper());

        if (null != converter) {
            UnderstandResult convert = converter.convert();
            Log.i("test", "response:" + gson.toJson(convert));
        }
    }

    private UnderstandResult.Intent getIntent(JSONObject root) {
        UnderstandResult.Intent.Builder builder = new UnderstandResult.Intent.Builder();

        try {
            JSONArray intents = root.getJSONArray("intent");
            JSONObject intentElement = new JSONObject();

            if (isContainKeyIntent(intents)) {
                builder.setScore(intentElement.getInt("score"));
                // 属于flight 意图
                //获取intent 数组里面的slot
                List<Pair<String, JSONArray>> slotParamPairs = getIntentSlotParamPairs(
                        intentElement);
                //todo 这里按照映射表 进行list 的映射处理

                for (Pair<String, JSONArray> p : slotParamPairs) {
                    String first = p.first;
                    JSONArray second = p.second;
                    builder.appendSlotsPair(first, second);
                }

                // 获取data下面的slot
                JSONArray data = root.getJSONArray("data");
                if (null != data && data.length() != 0) {
                    JSONObject jsonObject = data.optJSONObject(0);
                    List<Pair<String, String>> dataSlotParamPairs = getDataSlotParamPairs(
                            jsonObject);
                    for (Pair<String, String> p : dataSlotParamPairs) {
                        String first = p.first;
                        String second = p.second;
                        builder.appendSlotsPair(first, second);
                    }
                }
                return builder.build();
            }

            if (null != intents && intents.length() != 0) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    /**
     * if the intent:[...] contain "flight","..." some intents
     */
    private boolean isContainKeyIntent(JSONArray array) {
        List<String> keys = new ArrayList<String>() {
            {
                add("flight");
            }
        };

        if (null == array || array.length() == 0) {
            return false;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String value = jsonObject.getString("value");
                if (keys.contains(value)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @param jsonObject {
     *                   "type": "text",
     *                   "value": "flight",
     *                   "score": 90,
     *                   "category": "userDefine",
     *                   "data": {
     *                   "param": {
     *                   "entities_root>>city": [
     *                   "昆明"
     *                   ]
     *                   }
     *                   }
     *                   }
     */
    private List<Pair<String, JSONArray>> getIntentSlotParamPairs(JSONObject jsonObject)
            throws JSONException {
        List<Pair<String, JSONArray>> pairs = new ArrayList<>();
        JSONObject data = jsonObject.getJSONObject("data");
        if (null == data) {
            return pairs;
        }
        JSONObject param = data.getJSONObject("param");
        if (null == param) {
            return pairs;
        }
        Iterator<String> keys = param.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray jsonArray = param.optJSONArray(key);
            Pair<String, JSONArray> pair = new Pair<>(key, jsonArray);
            pairs.add(pair);
        }
        return pairs;
    }

    /**
     * {
     * "type": "customdata",
     * "cmd": "flight",
     * "value": "",
     * "data": {
     * "type": "flight",
     * "category": "优必选",
     * "atime": null,
     * "fnumb": null,
     * "atimeEndStandard": null,
     * "btime": null,
     * "btimeBeginStandard": null,
     * "atimeBeginStandard": null,
     * "btimeEndStandard": null,
     * "acity": "昆明",
     * "aport": null,
     * "bport": null,
     * "airline": null,
     * "bcity": "北京"
     * }
     * }
     */
    private List<Pair<String, String>> getDataSlotParamPairs(JSONObject jsonObject)
            throws JSONException {
        List<Pair<String, String>> pairs = new ArrayList<>();
        JSONObject data = jsonObject.getJSONObject("data");
        if (null == data) {
            return pairs;
        }

        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = data.optString(key);
            Pair<String, String> pair = new Pair<>(key, value);
            pairs.add(pair);
        }
        return pairs;
    }

    private UnderstandResult.Fulfillment getFullfillment(JSONObject jsonObject) {
        UnderstandResult.Fulfillment.Builder builder = new UnderstandResult.Fulfillment.Builder();
        List<UnderstandResult.Message> list = new ArrayList<>();
        try {
            JSONArray datas = jsonObject.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                UnderstandResult.Message.Builder messageBuilder =
                        new UnderstandResult.Message.Builder();
                JSONObject bean = datas.getJSONObject(i);

                if (bean != null) {
                    String type = bean.getString("type");
                    String string = bean.optString("value");
                    if ("text".equals(type) && !isJsonObject(string)) {
                        messageBuilder.setType(type);
                        //这里需要添加service 吗
                        messageBuilder.setPlatform("service" + i);
                        JSONObject jsonObject1 = new JSONObject();
                        String value = bean.getString("value");
                        if (isJsonObject(value)) {
                        } else {
                            jsonObject1.put("speech", value);
                            builder.setSpeech(value);
                        }
                        messageBuilder.setParameters(jsonObject1);
                    }
                    list.add(messageBuilder.build());
                }
            }
            builder.setMessages(list);
            return builder.build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public boolean isJsonObject(String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            new JSONObject(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("test", "bad json: " + json);
            return false;
        }
    }
}
