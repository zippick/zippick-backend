//package zippick;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectResponse;
//
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//public class S3UploadTest {
//
//  @Autowired
//  private S3Client s3Client;
//
//  private final String BUCKET_NAME = "bucket-name";
//
//  @Test
//  void uploadTextFile() {
//    String key = "test-upload-" + UUID.randomUUID() + ".txt";
//    String content = "Hello S3 from Java SDK v2!";
//
//    PutObjectRequest putRequest = PutObjectRequest.builder()
//        .bucket(BUCKET_NAME)
//        .key(key)
//        .contentType("text/plain")
//        .build();
//
//    PutObjectResponse response = s3Client.putObject(putRequest, RequestBody.fromString(content));
//
//    assertThat(response).isNotNull();
//    assertThat(response.eTag()).isNotEmpty();
//
//    String publicUrl = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + key;
//    System.out.println("업로드 성공: " + key);
//    System.out.println("퍼블릭 URL: " + publicUrl);
//  }
//
//}
