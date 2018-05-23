package com.ubtrobot.io;

import com.ubtrobot.io.ipc.IoProto;

public class IoConverters {

    private IoConverters() {
    }

    public static FileInfo toFileInfoPojo(IoProto.FileInfo fileInfo) {
        return new FileInfo.Builder().setName(fileInfo.getName()).setFormat(fileInfo.getFormat())
                .setLocalUri(fileInfo.getLocalUri()).setRemoteUrl(fileInfo.getRemoteUrl()).build();
    }

    public static IoProto.FileInfo toFileInfoProto(FileInfo fileInfo) {
        return IoProto.FileInfo.newBuilder().setName(fileInfo.getName())
                .setFormat(fileInfo.getFormat()).setLocalUri(fileInfo.getLocalUri())
                .setRemoteUrl(fileInfo.getRemoteUrl()).build();
    }
}
