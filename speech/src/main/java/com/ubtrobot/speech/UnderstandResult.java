package com.ubtrobot.speech;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ubtrobot.validate.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnderstandResult implements Parcelable {

    public static final UnderstandResult NULL = new UnderstandResult.Builder().build();

    private String sessionId = "";
    private String source = "";
    private String language = "";
    private boolean actionIncomplete;
    private Intent intent = Intent.NULL;
    private List<Context> contexts = new ArrayList<>();
    private Fulfillment fulfillment = Fulfillment.NULL;
    private String inputText = "";
    private Map<String, List<Message>> messageMap = null;

    protected UnderstandResult() {

    }

    protected UnderstandResult(Builder builder) {
        this.sessionId = builder.sessionId;
        this.source = builder.source;
        this.language = builder.language;
        this.actionIncomplete = builder.actionIncomplete;
        this.intent = builder.intent;
        this.contexts = builder.contexts;
        this.fulfillment = builder.fulfillment;
        this.inputText = builder.inputText;
    }

    public UnderstandResult(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionId);
        dest.writeString(source);
        dest.writeString(language);
        dest.writeInt(actionIncomplete ? 1 : 0);
        dest.writeParcelable(intent, flags);
        dest.writeList(contexts);
        dest.writeParcelable(fulfillment, flags);
        dest.writeString(inputText);
    }

    @SuppressWarnings("unchecked")
    public void readFromParcel(Parcel in) {
        sessionId = in.readString();
        source = in.readString();
        language = in.readString();
        actionIncomplete = (in.readInt() == 1) ? true : false;
        intent = in.readParcelable(Intent.class.getClassLoader());
        contexts = in.readArrayList(Context.class.getClassLoader());
        fulfillment = in.readParcelable(Fulfillment.class.getClassLoader());
        inputText = in.readString();
    }

    public static final Parcelable.Creator<UnderstandResult> CREATOR = new Parcelable
            .Creator<UnderstandResult>() {
        @Override
        public UnderstandResult createFromParcel(Parcel source) {
            return new UnderstandResult(source);
        }

        @Override
        public UnderstandResult[] newArray(int size) {
            return new UnderstandResult[size];
        }
    };

    public String getSessionId() {
        return sessionId;
    }

    public String getSource() {
        return source;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isActionIncomplete() {
        return actionIncomplete;
    }

    public Intent getIntent() {
        return intent;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    public Fulfillment getFulfillment() {
        return fulfillment;
    }

    public String getInputText() {
        return inputText;
    }

    public Message obtainMessage(String type) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        return obtainMessage(type, 0);
    }

    public Message obtainMessage(String type, int index) {
        if (null == messageMap) {
            buildMessageMap();
        }
        List<Message> messages = messageMap.get(type);
        if (null == messages || messages.isEmpty()) {
            return null;
        }

        if (index > messages.size() - 1) {
            index = messages.size() - 1;
        } else if (index < 0) {
            index = 0;
        }

        return messages.get(index);
    }

    public void buildMessageMap() {
        //先给messageMap赋值，防止一直为null
        messageMap = new HashMap<>();
        if (null == fulfillment) {
            return;
        }
        List<Message> messages = fulfillment.getMessages();
        for (Message message : messages) {
            String type = message.getType();
            if (messageMap.containsKey(type)) {
                messageMap.get(type).add(message);
            } else {
                List<Message> list = new ArrayList<>();
                list.add(message);
                messageMap.put(type, list);
            }
        }
    }

    /**
     * 意图类
     */
    public static class Intent implements Parcelable {

        public static final Intent NULL = new Intent.Builder().build();

        private String name;
        private String displayName;
        private JSONObject parameters;
        private float score;

        private Intent() {
        }

        public Intent(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(displayName);
            dest.writeString(parameters.toString());
            dest.writeFloat(score);

        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            name = in.readString();
            displayName = in.readString();
            String slotString = in.readString();
            try {
                parameters = new JSONObject(slotString);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new IllegalStateException("JSONException" + e.toString());
            }
            score = in.readFloat();
        }

        public static final Parcelable.Creator<Intent> CREATOR = new Parcelable
                .Creator<Intent>() {
            @Override
            public Intent createFromParcel(Parcel source) {
                return new Intent(source);
            }

            @Override
            public Intent[] newArray(int size) {
                return new Intent[size];
            }
        };

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public JSONObject getParameters() {
            return parameters;
        }

        public float getScore() {
            return score;
        }

        public final static class Builder {

            private String name = "";
            private String displayName = "";
            private JSONObject parameters = new JSONObject();
            private float score;

            public Builder() {
            }

            public Builder(Intent intent) {
                name = intent.name;
                displayName = intent.displayName;
                parameters = intent.parameters;
                score = intent.score;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setDisplayName(String displayName) {
                this.displayName = displayName;
                return this;
            }

            public Builder setParameters(JSONObject parameters) {
                this.parameters = parameters;
                return this;
            }

            public Builder appendSlotsPair(String key, Object value) {
                try {
                    parameters.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return this;
            }

            public Builder setScore(float score) {
                this.score = score;
                return this;
            }

            public Intent build() {
                UnderstandResult.Intent intent = new Intent();
                intent.name = name;
                intent.displayName = displayName;
                intent.parameters = parameters;
                intent.score = score;

                return intent;
            }
        }

        @Override
        public String toString() {
            return "Intent{" +
                    "name='" + name + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", parameters=" + parameters +
                    ", score=" + score +
                    '}';
        }
    }

    /**
     * 上下文类
     */
    public static class Context implements Parcelable {

        private String name = "";
        private JSONObject parameters = new JSONObject();
        private int lifespan;

        private Context() {
        }

        public Context(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(parameters.toString());
            dest.writeInt(lifespan);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            name = in.readString();
            String slotString = in.readString();
            try {
                parameters = new JSONObject(slotString);
            } catch (JSONException e) {

                e.printStackTrace();
            }
            lifespan = in.readInt();
        }

        public static final Parcelable.Creator<Context> CREATOR = new Parcelable
                .Creator<Context>() {
            @Override
            public Context createFromParcel(Parcel source) {
                return new Context(source);
            }

            @Override
            public Context[] newArray(int size) {
                return new Context[size];
            }
        };

        public String getName() {
            return name;
        }

        public JSONObject getSlots() {
            return parameters;
        }

        public int getLifespan() {
            return lifespan;
        }

        public static class Builder {

            private String name;
            private JSONObject parameters;
            private int lifespan;

            public Builder() {
            }

            public Builder(Context context) {
                Preconditions.checkNotNull(context,
                        "Context.Builder construct refuse null context");
                this.name = context.name;
                this.parameters = context.parameters;
                this.lifespan = context.lifespan;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setParameters(JSONObject parameters) {
                this.parameters = parameters;
                return this;
            }

            public Builder setLifespan(int lifespan) {
                this.lifespan = lifespan;
                return this;
            }

            public Context build() {
                Context context = new Context();
                context.name = name;
                context.parameters = parameters;
                context.lifespan = lifespan;
                return context;
            }
        }

        @Override
        public String toString() {
            return "Context{" +
                    "name='" + name + '\'' +
                    ", parameters=" + parameters +
                    ", lifespan=" + lifespan +
                    '}';
        }
    }

    /**
     * 业务数据类
     */
    public static class Fulfillment implements Parcelable {

        public static final Fulfillment NULL = new Fulfillment.Builder().build();

        private String speech = "";
        private List<Message> messages = new ArrayList<>();
        private Status status;

        private Fulfillment() {
        }

        public Fulfillment(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(speech);
            dest.writeList(messages);
            dest.writeParcelable(status, flags);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            speech = in.readString();
            messages = in.readArrayList(Message.class.getClassLoader());
            status = in.readParcelable(Status.class.getClassLoader());
        }

        public static final Parcelable.Creator<Fulfillment> CREATOR = new Parcelable
                .Creator<Fulfillment>() {
            @Override
            public Fulfillment createFromParcel(Parcel source) {
                return new Fulfillment(source);
            }

            @Override
            public Fulfillment[] newArray(int size) {
                return new Fulfillment[size];
            }
        };

        public String getSpeech() {
            return speech;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public Status getStatus() {
            return status;
        }

        public Builder toBuilder() {
            return new Builder(this);
        }

        public static class Builder {

            private String speech = "";
            private List<Message> messages = new ArrayList<>();
            private Status status = Status.NULL;

            public Builder() {
            }

            public Builder(Fulfillment fulfillment) {
                Preconditions.checkNotNull(fulfillment,
                        "Fulfillment。Builder refuse null Fulfillment");
                this.speech = fulfillment.speech;
                //todo 是不是希望可以把message也拷贝了
                List<UnderstandResult.Message> copy = new ArrayList<>(fulfillment.messages);
                this.messages = copy;
                this.status = fulfillment.status;
            }

            public Builder setSpeech(String speech) {
                if (speech == null) {
                    throw new IllegalArgumentException("Fulfillment.Builder refuse null speech");
                }
                this.speech = speech;
                return this;
            }

            public Builder setMessages(List<Message> messages) {
                this.messages = messages;
                return this;
            }

            public Builder setStatus(Status status) {
                this.status = status;
                return this;
            }

            public Fulfillment build() {
                Fulfillment fulfillment = new Fulfillment();
                fulfillment.speech = speech;
                fulfillment.messages = messages;
                fulfillment.status = status;
                return fulfillment;
            }
        }

        @Override
        public String toString() {
            return "Fulfillment{" +
                    "speech='" + speech + '\'' +
                    ", messages=" + messages +
                    ", status=" + status +
                    '}';
        }
    }

    public static class Status implements Parcelable {

        public static final Status NULL = new Status.Builder().build();

        private int code;
        private String errorMessage = "";
        private String errorDetails = "";

        private Status() {
        }

        public Status(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(code);
            dest.writeString(errorMessage);
            dest.writeString(errorDetails);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            code = in.readInt();
            errorMessage = in.readString();
            errorDetails = in.readString();
        }

        public static final Parcelable.Creator<Status> CREATOR = new Parcelable
                .Creator<Status>() {
            @Override
            public Status createFromParcel(Parcel source) {
                return new Status(source);
            }

            @Override
            public Status[] newArray(int size) {
                return new Status[size];
            }
        };

        public int getCode() {
            return code;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorDetails() {
            return errorDetails;
        }

        public static class Builder {

            private int code;
            private String errorMessage = "";
            private String errorDetails = "";

            public Builder setCode(int code) {
                this.code = code;
                return this;
            }

            public Builder setErrorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }

            public Builder setErrorDetails(String errorDetails) {
                this.errorDetails = errorDetails;
                return this;
            }

            public Status build() {
                Status status = new Status();
                status.code = code;
                status.errorDetails = errorDetails;
                status.errorMessage = errorMessage;
                return status;
            }
        }

        @Override
        public String toString() {
            return "Status{" +
                    "code=" + code +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", errorDetails='" + errorDetails + '\'' +
                    '}';
        }
    }

    /**
     * "messages": [
     * {
     * "type": 0,
     * "platform": "facebook",
     * "speech": "facebook"
     * },
     * {
     * "type": 0,
     * "speech": "bejing sunny sunny"
     * },
     * {
     * "type": 0,
     * "speech": "beijing sunny"
     * },
     * {
     * "type": 4,
     * "payload": {
     * "dan": "sdasd"
     * }
     * }
     * ]
     */
    public static class Message implements Parcelable {

        public static final Message NULL = new Message.Builder().build();

        private String type = "";
        private String platform = "";
        private JSONObject parameters = new JSONObject();

        private Message() {
        }

        public Message(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(platform);
            dest.writeString(parameters.toString());
        }


        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            type = in.readString();
            platform = in.readString();
            String paramString = in.readString();
            try {
                parameters = new JSONObject(paramString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static final Parcelable.Creator<Message> CREATOR = new Parcelable
                .Creator<Message>() {
            @Override
            public Message createFromParcel(Parcel source) {
                return new Message(source);
            }

            @Override
            public Message[] newArray(int size) {
                return new Message[size];
            }
        };

        public String getType() {
            return type;
        }

        public String getPlatform() {
            return platform;
        }

        public JSONObject getParameters() {
            return parameters;
        }

        public Builder toBuilder() {
            return new Builder(this);
        }

        public static class Builder {
            public static final String TYPE_ORIGINAL = "original";
            public static final String TYPE_TEXT = "text";
            public static final String TYPE_USERDEFINE = "userDefine";
            private String type = "";
            private String platform = "";
            private JSONObject parameters = new JSONObject();

            public Builder() {
            }

            public Builder(Message message) {
                Preconditions.checkNotNull(message, "Message.Builder refuse null message");
                this.type = message.type;
                this.platform = message.platform;
                try {
                    JSONObject copy = new JSONObject(parameters.toString());
                    this.parameters = copy;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public Builder setType(String type) {
                this.type = type;
                return this;
            }

            public Builder setPlatform(String platform) {
                this.platform = platform;
                return this;
            }

            public Builder setParameters(JSONObject parameters) {
                this.parameters = parameters;
                return this;
            }

            public Message build() {
                Message message = new Message();
                message.type = type;
                message.platform = platform;
                message.parameters = parameters;

                return message;
            }
        }

    }

    public static class Builder {

        private String sessionId = "";
        private List<Intent> intentCandidates = new ArrayList<>();
        private String source = "";
        private String language = "";
        private boolean actionIncomplete;
        private Intent intent = Intent.NULL;
        private List<Context> contexts = new ArrayList<>();
        private Fulfillment fulfillment = Fulfillment.NULL;
        private String inputText = "";

        public Builder() {

        }

        public Builder(UnderstandResult result) {
            Preconditions.checkNotNull(result,
                    "UnderstandResult.Builder refuse null UnderstandResult");
            this.sessionId = result.sessionId;
            this.source = result.source;
            this.language = result.language;
            this.actionIncomplete = result.actionIncomplete;
            this.intent = result.intent;
            this.contexts = result.contexts;
            this.fulfillment = result.fulfillment;
            this.inputText = result.inputText;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder setIntentCandidates(
                List<UnderstandResult.Intent> intentCandidates) {
            this.intentCandidates = intentCandidates;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setActionIncomplete(boolean actionIncomplete) {
            this.actionIncomplete = actionIncomplete;
            return this;
        }

        public Builder setIntent(UnderstandResult.Intent intent) {
            this.intent = intent;
            return this;
        }

        public Builder setContexts(List<Context> contexts) {
            this.contexts = contexts;
            return this;
        }

        public Builder setFulfillment(Fulfillment fulfillment) {
            this.fulfillment = fulfillment;
            return this;
        }

        public Builder setInputText(String inputText) {
            this.inputText = inputText;
            return this;
        }

        //EmotibotConvert need get InputText from this builder
        public String getInputText() {
            return inputText;
        }

        public UnderstandResult build() {
            UnderstandResult understandResult = new UnderstandResult();
            understandResult.sessionId = sessionId;
            understandResult.source = source;
            understandResult.language = language;
            understandResult.actionIncomplete = actionIncomplete;
            understandResult.intent = intent;
            understandResult.contexts = contexts;
            understandResult.fulfillment = fulfillment;
            understandResult.inputText = inputText;

            return understandResult;
        }
    }

    @Override
    public String toString() {
        return "UnderstandResult{" +
                "sessionId='" + sessionId + '\'' +
                ", source='" + source + '\'' +
                ", language='" + language + '\'' +
                ", actionIncomplete=" + actionIncomplete +
                ", intent=" + intent +
                ", contexts=" + contexts +
                ", fulfillment=" + fulfillment +
                ", inputText='" + inputText + '\'' +
                '}';
    }
}
