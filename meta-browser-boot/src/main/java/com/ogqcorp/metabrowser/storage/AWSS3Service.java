package com.ogqcorp.metabrowser.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AWSS3Service {
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Async
    public String store(Path path, String staticDir) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, staticDir, path.toFile()).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, staticDir).toString();
    }


    @Async
    public String store(Path path, String staticDir, Function<Path, Boolean> function) {

        String storeUrl = store(path, staticDir);
        function.apply(path);

        return storeUrl;
    }
}
