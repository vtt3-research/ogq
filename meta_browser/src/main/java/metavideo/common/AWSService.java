package metavideo.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;


/*
출처: http://dwfox.tistory.com/57
* */
public class AWSService {
    private static final String BUCKET_NAME = "vtt-ogq";
    private static final String ACCESS_KEY = "AKIAJHI253AC5AXT52GQ";
    private static final String SECRET_KEY = "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";

    /*
    bucket name: vtt-ogq (seoul)
    access key: AKIAJHI253AC5AXT52GQ
    secret: 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
    */

    public static void transferFile(File file) {
        //        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
        AmazonS3 amazonS3 = null;
        PutObjectRequest putObjectRequest = null;
        amazonS3 = new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));


        if (amazonS3 != null) {
            try {
                putObjectRequest = new PutObjectRequest(BUCKET_NAME, file.getName(), file);
                putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // file permission
                System.out.println("1");
                amazonS3.putObject(putObjectRequest); // upload file
                System.out.println("9");

            } catch (AmazonServiceException ase) {
                ase.printStackTrace();
            } catch (AmazonClientException ace) {
                ace.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                amazonS3 = null;
            }
        }
    }

    public static void transferFile(String filePath) {
        File file=new File(filePath);
        //        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
        AmazonS3 amazonS3 = null;
        PutObjectRequest putObjectRequest = null;

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        amazonS3 = new AmazonS3Client(basicAWSCredentials);

        if (amazonS3 != null) {
            try {
                putObjectRequest = new PutObjectRequest(BUCKET_NAME, file.getName(), file);
                putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // file permission
                System.out.println("1");
                amazonS3.putObject(putObjectRequest); // upload file
                System.out.println("9");

            } catch (AmazonServiceException ase) {
                ase.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                amazonS3 = null;
            }
        }
    }

    public static void transferFile(String fromPath, String directory, String fileName) {
        File file=new File(fromPath);
        //        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
        AmazonS3 amazonS3 = null;
        PutObjectRequest putObjectRequest = null;

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        amazonS3 = new AmazonS3Client(basicAWSCredentials);

        if (amazonS3 != null) {
            try {
                putObjectRequest = new PutObjectRequest(BUCKET_NAME, directory+"/"+fileName, file);
                putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // file permission
                System.out.println("1");
                amazonS3.putObject(putObjectRequest); // upload file
                System.out.println("9");

            } catch (AmazonServiceException ase) {
                ase.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                amazonS3 = null;
            }
        }
    }

    public static void transferMetaFile(String fromPath, String fileName) {
        transferFile(fromPath, "meta_attach", fileName);
    }

    public static void transferVideoFile(String fromPath, String fileName) {
        transferFile(fromPath, "movie_attach", fileName);
    }



}

