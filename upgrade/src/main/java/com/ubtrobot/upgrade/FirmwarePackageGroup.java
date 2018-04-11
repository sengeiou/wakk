package com.ubtrobot.upgrade;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 固件包组
 */
public class FirmwarePackageGroup implements Iterable<FirmwarePackage> {

    public static final FirmwarePackageGroup DEFAULT = new FirmwarePackageGroup.Builder("").build();

    private String name;
    private boolean forced;
    private long totalSize;
    private long releaseTime;
    private String releaseNote;
    private LinkedList<FirmwarePackage> packageList = new LinkedList<>();

    private FirmwarePackageGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isForced() {
        return forced;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public int getPackageCount() {
        return packageList.size();
    }

    public FirmwarePackage getPackage(int index) {
        return packageList.get(index);
    }

    @NonNull
    @Override
    public Iterator<FirmwarePackage> iterator() {
        final Iterator<FirmwarePackage> iterator = packageList.iterator();
        return new Iterator<FirmwarePackage>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public FirmwarePackage next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwarePackageGroup group = (FirmwarePackageGroup) o;

        if (forced != group.forced) return false;
        if (releaseTime != group.releaseTime) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        if (releaseNote != null ? !releaseNote.equals(group.releaseNote) : group.releaseNote != null)
            return false;
        return packageList != null ? packageList.equals(group.packageList) : group.packageList == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (forced ? 1 : 0);
        result = 31 * result + (int) (releaseTime ^ (releaseTime >>> 32));
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0);
        result = 31 * result + (packageList != null ? packageList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FirmwarePackageGroup{" +
                "name='" + name + '\'' +
                ", forced=" + forced +
                ", totalSize=" + totalSize +
                ", releaseTime=" + releaseTime +
                ", releaseNote='" + releaseNote + '\'' +
                ", packageList=" + packageList +
                '}';
    }

    public static class Builder {

        private final String groupName;
        private boolean forced;
        private long totalSize;
        private long releaseTime;
        private String releaseNote = "";
        private LinkedList<FirmwarePackage> packages = new LinkedList<>();

        private final HashSet<String> packageNameSet = new HashSet<>();

        public Builder(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("Argument groupName is null.");
            }

            this.groupName = groupName;
        }

        public Builder setForced(boolean forced) {
            this.forced = forced;
            return this;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public Builder setReleaseTime(long releaseTime) {
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

        public Builder addPackage(FirmwarePackage firmwarePackage) {
            if (firmwarePackage == null) {
                throw new IllegalArgumentException("Argument firmwarePackage is null.");
            }

            if (!groupName.equals(firmwarePackage.getGroup())) {
                throw new IllegalArgumentException("Illegal argument firmwarePackage. " +
                        "firmwarePackage.group NOT equal " + groupName);
            }

            if (packageNameSet.contains(firmwarePackage.getName())) {
                throw new IllegalArgumentException("Illegal argument firmwarePackage. The package " +
                        "is already in the group. packageName=" + firmwarePackage.getName());
            }

            packages.add(firmwarePackage);
            packageNameSet.add(firmwarePackage.getName());

            if (firmwarePackage.isIncremental()) {
                totalSize += firmwarePackage.getIncrementSize();
            } else {
                totalSize += firmwarePackage.getPackageSize();
            }

            return this;
        }

        public FirmwarePackageGroup build() {
            FirmwarePackageGroup group = new FirmwarePackageGroup(groupName);
            group.forced = forced;
            group.totalSize = totalSize;
            group.releaseTime = releaseTime;
            group.releaseNote = releaseNote;
            group.packageList.addAll(packages);

            return group;
        }
    }
}
