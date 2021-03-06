package com.ubtrobot.upgrade;

/**
 * 固件包
 */
public class FirmwarePackage {

    public static final FirmwarePackage DEFAULT = new FirmwarePackage.Builder("", "").build();

    private String group;
    private String name;
    private String version;
    private boolean forced;
    private boolean incremental;
    private String packageUrl;
    private long packageSize;
    private String packageMd5;
    private String incrementUrl;
    private long incrementSize;
    private String incrementMd5;
    private long releaseTime;
    private String releaseNote;
    private String localFile;

    private FirmwarePackage(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isForced() {
        return forced;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public long getPackageSize() {
        return packageSize;
    }

    public String getPackageMd5() {
        return packageMd5;
    }

    public String getIncrementUrl() {
        return incrementUrl;
    }

    public long getIncrementSize() {
        return incrementSize;
    }

    public String getIncrementMd5() {
        return incrementMd5;
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public String getLocalFile() {
        return localFile;
    }

    public Builder newBuilder() {
        return new Builder(name, version).setGroup(group).setForced(forced).
                setIncremental(incremental).setPackageUrl(packageUrl).setPackageSize(packageSize).
                setPackageMd5(packageMd5).setIncrementUrl(incrementUrl).
                setIncrementSize(incrementSize).setIncrementMd5(incrementMd5).
                setReleaseTime(releaseTime).setReleaseNote(releaseNote).setLocalFile(localFile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwarePackage that = (FirmwarePackage) o;

        if (forced != that.forced) return false;
        if (incremental != that.incremental) return false;
        if (releaseTime != that.releaseTime) return false;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (packageUrl != null ? !packageUrl.equals(that.packageUrl) : that.packageUrl != null)
            return false;
        if (packageMd5 != null ? !packageMd5.equals(that.packageMd5) : that.packageMd5 != null)
            return false;
        if (incrementUrl != null ? !incrementUrl.equals(that.incrementUrl) : that.incrementUrl != null)
            return false;
        if (incrementMd5 != null ? !incrementMd5.equals(that.incrementMd5) : that.incrementMd5 != null)
            return false;
        if (releaseNote != null ? !releaseNote.equals(that.releaseNote) : that.releaseNote != null)
            return false;
        return localFile != null ? localFile.equals(that.localFile) : that.localFile == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (forced ? 1 : 0);
        result = 31 * result + (incremental ? 1 : 0);
        result = 31 * result + (packageUrl != null ? packageUrl.hashCode() : 0);
        result = 31 * result + (packageMd5 != null ? packageMd5.hashCode() : 0);
        result = 31 * result + (incrementUrl != null ? incrementUrl.hashCode() : 0);
        result = 31 * result + (incrementMd5 != null ? incrementMd5.hashCode() : 0);
        result = 31 * result + (int) (releaseTime ^ (releaseTime >>> 32));
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0);
        result = 31 * result + (localFile != null ? localFile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FirmwarePackage{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", forced=" + forced +
                ", incremental=" + incremental +
                ", packageUrl='" + packageUrl + '\'' +
                ", packageMd5='" + packageMd5 + '\'' +
                ", incrementUrl='" + incrementUrl + '\'' +
                ", incrementMd5='" + incrementMd5 + '\'' +
                ", releaseTime=" + releaseTime +
                ", releaseNote='" + releaseNote + '\'' +
                ", localFile='" + localFile + '\'' +
                '}';
    }

    public static class Builder {

        private String group = "";
        private final String name;
        private final String version;
        private boolean forced;
        private boolean incremental;
        private String packageUrl = "";
        private long packageSize;
        private String packageMd5 = "";
        private String incrementUrl = "";
        private long incrementSize;
        private String incrementMd5 = "";
        private long releaseTime;
        private String releaseNote = "";
        private String localFile = "";

        public Builder(String name, String version) {
            if (name == null || version == null) {
                throw new IllegalArgumentException("Argument name or version is null.");
            }

            this.name = name;
            this.version = version;
        }

        public Builder setGroup(String group) {
            if (group == null) {
                throw new IllegalArgumentException("Argument group is null.");
            }

            this.group = group;
            return this;
        }

        public Builder setForced(boolean forced) {
            this.forced = forced;
            return this;
        }

        public Builder setIncremental(boolean incremental) {
            this.incremental = incremental;
            return this;
        }

        public Builder setPackageUrl(String packageUrl) {
            if (packageUrl == null) {
                throw new IllegalArgumentException("Argument packageUrl is null.");
            }

            this.packageUrl = packageUrl;
            return this;
        }

        public Builder setPackageSize(long packageSize) {
            this.packageSize = packageSize;
            return this;
        }

        public Builder setPackageMd5(String packageMd5) {
            if (packageMd5 == null) {
                throw new IllegalArgumentException("Argument packageMd5 is null.");
            }

            this.packageMd5 = packageMd5;
            return this;
        }

        public Builder setIncrementUrl(String incrementUrl) {
            if (incrementUrl == null) {
                throw new IllegalArgumentException("Argument incrementUrl is null.");
            }

            this.incrementUrl = incrementUrl;
            return this;
        }

        public Builder setIncrementSize(long incrementSize) {
            this.incrementSize = incrementSize;
            return this;
        }

        public Builder setIncrementMd5(String incrementMd5) {
            if (incrementMd5 == null) {
                throw new IllegalArgumentException("Argument incrementMd5 is null.");
            }

            this.incrementMd5 = incrementMd5;
            return this;
        }

        public Builder setReleaseTime(long releaseTime) {
            if (releaseTime < 0) {
                throw new IllegalArgumentException("Argument releaseTime < 0");
            }

            this.releaseTime = releaseTime;
            return this;
        }

        public Builder setReleaseNote(String releaseNote) {
            if (releaseNote == null) {
                throw new IllegalArgumentException("Argument releaseNote is null.");
            }

            this.releaseNote = releaseNote;
            return this;
        }

        public Builder setLocalFile(String localFile) {
            if (localFile == null) {
                throw new IllegalArgumentException("Argument localFile is null.");
            }

            this.localFile = localFile;
            return this;
        }

        public FirmwarePackage build() {
            FirmwarePackage firmwarePackage = new FirmwarePackage(name, version);
            firmwarePackage.group = group;
            firmwarePackage.forced = forced;
            firmwarePackage.incremental = incremental;
            firmwarePackage.packageUrl = packageUrl;
            firmwarePackage.packageSize = packageSize;
            firmwarePackage.packageMd5 = packageMd5;
            firmwarePackage.incrementUrl = incrementUrl;
            firmwarePackage.incrementSize = incrementSize;
            firmwarePackage.incrementMd5 = incrementMd5;
            firmwarePackage.releaseTime = releaseTime;
            firmwarePackage.releaseNote = releaseNote;
            firmwarePackage.localFile = localFile;

            return firmwarePackage;
        }
    }
}