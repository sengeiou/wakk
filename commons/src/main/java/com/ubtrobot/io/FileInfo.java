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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (name != null ? !name.equals(fileInfo.name) : fileInfo.name != null) return false;
        if (format != null ? !format.equals(fileInfo.format) : fileInfo.format != null)
            return false;
        if (remoteUrl != null ? !remoteUrl.equals(fileInfo.remoteUrl) : fileInfo.remoteUrl != null)
            return false;
        return localUri != null ? localUri.equals(fileInfo.localUri) : fileInfo.localUri == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (remoteUrl != null ? remoteUrl.hashCode() : 0);
        result = 31 * result + (localUri != null ? localUri.hashCode() : 0);
        return result;
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

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private String name;
        private String format;
        private String remoteUrl;
        private String localUri;

        public Builder() {
        }

        public Builder(FileInfo fileInfo) {
            name = fileInfo.name;
            format = fileInfo.format;
            remoteUrl = fileInfo.remoteUrl;
            localUri = fileInfo.localUri;
        }

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
