package com.example.test.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지를 S3에 업로드하고 URL 반환
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 원본 파일명
        String originalFilename = file.getOriginalFilename();

        // UUID로 고유한 파일명 생성 (중복 방지)
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 업로드
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

        // 업로드된 파일의 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * S3에서 이미지 삭제
     */
    public void deleteImage(String fileUrl) {
        // URL에서 파일명 추출
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3.deleteObject(bucket, fileName);
    }
}