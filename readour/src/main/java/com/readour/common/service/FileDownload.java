package com.readour.common.service;

import com.readour.common.entity.FileAsset;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Getter
@RequiredArgsConstructor
public class FileDownload implements AutoCloseable {

    private final FileAsset asset;
    private final ResponseInputStream<GetObjectResponse> inputStream;

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
