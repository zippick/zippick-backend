package zippick.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

  /*// 기본
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.AP_NORTHEAST_2) // 서울 리전
        .build();
  }*/

  // 로컬
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(ProfileCredentialsProvider.create("zippick"))
            .build();
  }

}

