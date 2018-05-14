package com.ubtrobot.speech.understand;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UnderstandResult implements Parcelable {

    private String sessionId;
    private String source;
    private String language;
    private boolean actionIncomplete;
    private Intent intent;
    private List<Context> contexts;
    private Fulfillment fulfillment;
    private String inputText;

    private UnderstandResult() {
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
    private void readFromParcel(Parcel in) {
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

    /**
     * 意图类
     */
    public static class Intent implements Parcelable {

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

            private String name;
            private String displayName;
            private JSONObject parameters = new JSONObject();
            private float score;

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

    }

    /**
     * 上下文类
     */
    public static class Context implements Parcelable {

        private String name;
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
    }

    /**
     * 业务数据类
     */
    public static class Fulfillment implements Parcelable {

        private String speech;
        private List<Message> messages;
        //老版的时候出现这个字段
        private Message legacyMessage;
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
            dest.writeParcelable(legacyMessage, flags);
            dest.writeParcelable(status, flags);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            speech = in.readString();
            messages = in.readArrayList(Message.class.getClassLoader());
            legacyMessage = in.readParcelable(Message.class.getClassLoader());
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

        public static class Builder {

            private String speech;
            private List<Message> messages = new ArrayList<>();
            private Message legacyMessage;
            private Status status;

            public Builder setSpeech(String speech) {
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

            public Builder setLegacyMessage(Message legacyMessage) {
                this.legacyMessage = legacyMessage;
                return this;
            }

            public Fulfillment build() {
                Fulfillment fulfillment = new Fulfillment();
                fulfillment.speech = speech;
                fulfillment.messages = messages;
                fulfillment.legacyMessage = legacyMessage;
                fulfillment.status = status;
                return fulfillment;
            }
        }
    }

    public static class Status implements Parcelable {
        private int code;
        private String errorMessage;
        private String errorDetails;

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
            private String errorMessage;
            private String errorDetails;

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

        private String type;
        private String platform;
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

        public static class Builder {
            public static final String TYPE_ORIGINAL = "original";
            public static final String TYPE_TEXT = "text";
            private String type;
            private String platform;
            private JSONObject parameters = new JSONObject();

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

        private String sessionId;
        private List<Intent> intentCandidates;
        private String source;
        private String language;
        private boolean actionIncomplete;
        private Intent intent;
        private List<Context> contexts;
        private Fulfillment fulfillment;
        private String inputText;

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
}
