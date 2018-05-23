package com.ubtrobot.io;

public class FileInfo {

    public static final FileInfo DEFAULT = new FileInfo.Builder().build();

    private String name;
    private String format;
    private String remoteUrl;
    private String localUri;

    private FileInfo() {
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getLocalUri() {
        return localUri;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", format='" + format + '\'' +
                ", remoteUrl='" + remoteUrl + '\'' +
                ", localUri='" + localUri + '\'' +
                '}';
    }

    public static class Builder {

        private String name;
        private String format;
        private String remoteUrl;
        private String localUri;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setRemoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
            return this;
        }

        public Builder setLocalUri(String localUri) {
            this.localUri = localUri;
            return this;
        }

        public FileInfo build() {
            FileInfo fileInfo = new FileInfo();
            fileInfo.name = name == null ? "" : name;
            fileInfo.format = format == null ? "" : format;
            fileInfo.remoteUrl = remoteUrl == null ? "" : remoteUrl;
            fileInfo.localUri = localUri == null ? "" : localUri;

            return fileInfo;
        }
    }
}
