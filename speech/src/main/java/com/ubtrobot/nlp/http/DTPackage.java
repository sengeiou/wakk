package com.ubtrobot.nlp.http;

import com.google.gson.Gson;
import com.ubtrobot.speech.understand.UnderstandResult;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DTPackage {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DTPackage");
    /**
     * status : 200
     * message :
     * sessionId :
     * intentCandidates : []
     * nlpProvider : 2
     * nluData : {"feedback":[],"semantic":{"slots":{"from":["上海"],"to":["北京"]},"emotion":[],
     * "person":[],"domain":[],"actionObject":[],"location":[],"logic":[],"time":[],
     * "intent":["flight"],"entity":[],"actionSubject":[]},"inputText":"上海到北京的机票"}
     * data : {"type":"text","subject":{},"content":""}
     * originalMessage : {"return":0,"return_message":"","status":0,"data":[{"type":"customdata",
     * "cmd":"flight","value":"","data":{"type":"flight","category":"优必选","from":"上海",
     * "to":"北京"}}],"emotion":[{"type":"text","value":"中性","score":"79.000000","category":null,
     * "data":[]}],"intent":[{"type":"text","value":"flight","score":94,"category":"userDefine",
     * "data":{}}]}
     */

    private String status;
    private String message;
    private String sessionId;
    private String nlpProvider;
    private NluDataBean nluData;
    private DataBean data;
    private String originalMessage;
    private List<String> intentCandidates;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNlpProvider() {
        return nlpProvider;
    }

    public void setNlpProvider(String nlpProvider) {
        this.nlpProvider = nlpProvider;
    }

    public NluDataBean getNluData() {
        return nluData;
    }

    public void setNluData(NluDataBean nluData) {
        this.nluData = nluData;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public List<String> getIntentCandidates() {
        return intentCandidates;
    }

    public void setIntentCandidates(List<String> intentCandidates) {
        this.intentCandidates = intentCandidates;
    }

    public UnderstandResult convertToUnderstandResult() {
        Gson gson = new Gson();

        UnderstandResult.Builder builder = new UnderstandResult.Builder();

        builder.setSessionId(getSessionId());
        builder.setSource(getNlpProvider());
        //todo 目前DTP没有language字段
        builder.setLanguage("");
        //todo 目前DTP没有actionIncomplete字段
        builder.setActionIncomplete(false);

        builder.setIntentCandidates(new ArrayList<UnderstandResult.Intent>());

        builder.setInputText(getNluData().inputText);

        UnderstandResult.Intent.Builder intentBuilder = new UnderstandResult.Intent.Builder();

        //============================Intent====================
        //填充intent.name
        List<String> intents = getNluData().getSemantic().getIntent();
        if (intents != null && !intents.isEmpty()) {
            intentBuilder.setName(intents.get(0));
        } else {
            intentBuilder.setName("");
        }
        //填充intent.slot
        JSONObject slots = getNluData().getSemantic().getSlots();
        intentBuilder.setParameters(slots);

        //todo 这里action需要后台调整格式在semantic里面添加一个
        intentBuilder.setDisplayName("");

        builder.setIntent(intentBuilder.build());

        //============================Contexts==================
        //todo 向max确认context字段
        List<String> intentCandidates = getIntentCandidates();
        List<UnderstandResult.Context> contexts = new ArrayList<>();

        for (String string : intentCandidates) {
            //todo 这里context格式不明，需要调试修改此处
            UnderstandResult.Context.Builder contextBuilder =
                    new UnderstandResult.Context.Builder();
            contextBuilder.setName("context");
            JSONObject json = new JSONObject();
            try {
                json.put("param", "need check");
            } catch (JSONException e) {
                LOGGER.e("create json object error");
                e.printStackTrace();
            }
            contextBuilder.setParameters(json);
            contexts.add(contextBuilder.build());
        }
        builder.setContexts(contexts);

        //============================Fulfillment===============
        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        //todo 这里获取getContent
        fulfillmentBuilder.setSpeech(getData().getContent());

        List<UnderstandResult.Message> messages = new ArrayList<>();

        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        //todo nlp 格式为定义type
        // messageBuilder.setType(getData().getType());
        //todo nlp 格式未定义platform （业务数据来源于）
        messageBuilder.setPlatform("platform");
        messageBuilder.setParameters(getData().getSubject());
        messages.add(messageBuilder.build());
        fulfillmentBuilder.setMessages(messages);

        builder.setFulfillment(fulfillmentBuilder.build());
        return null;
    }


    public static class NluDataBean {
        /**
         * feedback : []
         * semantic : {"slots":{"from":["上海"],"to":["北京"]},"emotion":[],"person":[],"domain":[],
         * "actionObject":[],"location":[],"logic":[],"time":[],"intent":["flight"],"entity":[],
         * "actionSubject":[]}
         * inputText : 上海到北京的机票
         */

        private SemanticBean semantic;
        private String inputText;
        private List<String> feedback;

        public SemanticBean getSemantic() {
            return semantic;
        }

        public void setSemantic(SemanticBean semantic) {
            this.semantic = semantic;
        }

        public String getInputText() {
            return inputText;
        }

        public void setInputText(String inputText) {
            this.inputText = inputText;
        }

        public List<String> getFeedback() {
            return feedback;
        }

        public void setFeedback(List<String> feedback) {
            this.feedback = feedback;
        }

        public static class SemanticBean {
            /**
             * slots : {"from":["上海"],"to":["北京"]}
             * emotion : []
             * person : []
             * domain : []
             * actionObject : []
             * location : []
             * logic : []
             * time : []
             * intent : ["flight"]
             * entity : []
             * actionSubject : []
             */

            private JSONObject slots;
            private List<String> emotion;
            private List<String> person;
            private List<String> domain;
            private List<String> actionObject;
            private List<String> location;
            private List<String> logic;
            private List<String> time;
            private List<String> intent;
            private List<String> entity;
            private List<String> actionSubject;

            public JSONObject getSlots() {
                return slots;
            }

            public void setSlots(JSONObject slots) {
                this.slots = slots;
            }

            public List<String> getEmotion() {
                return emotion;
            }

            public void setEmotion(List<String> emotion) {
                this.emotion = emotion;
            }

            public List<String> getPerson() {
                return person;
            }

            public void setPerson(List<String> person) {
                this.person = person;
            }

            public List<String> getDomain() {
                return domain;
            }

            public void setDomain(List<String> domain) {
                this.domain = domain;
            }

            public List<String> getActionObject() {
                return actionObject;
            }

            public void setActionObject(List<String> actionObject) {
                this.actionObject = actionObject;
            }

            public List<String> getLocation() {
                return location;
            }

            public void setLocation(List<String> location) {
                this.location = location;
            }

            public List<String> getLogic() {
                return logic;
            }

            public void setLogic(List<String> logic) {
                this.logic = logic;
            }

            public List<String> getTime() {
                return time;
            }

            public void setTime(List<String> time) {
                this.time = time;
            }

            public List<String> getIntent() {
                return intent;
            }

            public void setIntent(List<String> intent) {
                this.intent = intent;
            }

            public List<String> getEntity() {
                return entity;
            }

            public void setEntity(List<String> entity) {
                this.entity = entity;
            }

            public List<String> getActionSubject() {
                return actionSubject;
            }

            public void setActionSubject(List<String> actionSubject) {
                this.actionSubject = actionSubject;
            }
        }
    }

    public static class DataBean {
        /**
         * type : text
         * subject : {}
         * content :
         */

        private String type;
        private JSONObject subject;
        private String content;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public JSONObject getSubject() {
            return subject;
        }

        public void setSubject(JSONObject subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public static class SubjectBean {
        }
    }
}
