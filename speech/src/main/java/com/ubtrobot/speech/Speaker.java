package com.ubtrobot.speech;

public class Speaker {

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    /**
     * id和语音引擎里发音人的索引id
     */
    private String id;

    /**
     * 可修改的名称，用于对外显示
     */
    private String name;
    private int gender;

    private Speaker(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Speaker{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                '}';
    }

    public static class Builder {

        private String id = "";
        private String name = "";
        private int gender;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setName(String name) {
            checkName(name);
            this.name = name;
            return this;
        }

        private void checkName(String name) {
            if (null == name) {
                throw new IllegalArgumentException("Speaker.Builder refuse name == null");
            }
        }

        public Builder setGender(int gender) {
            checkGender(gender);
            this.gender = gender;
            return this;
        }

        private void checkGender(int gender) {
            if (gender < GENDER_MALE && gender > GENDER_FEMALE) {
                throw new IllegalArgumentException("Invalid gender value, verify for Speaker.GENDER_xxx.");
            }
        }

        public Speaker build() {
            Speaker speaker = new Speaker(id);
            speaker.name = name;
            speaker.gender = gender;
            return speaker;
        }

    }
}
