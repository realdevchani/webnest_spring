package com.app.webnest.api.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file/*")
@Slf4j
@RequiredArgsConstructor
public class FileApi {

    @Value("${file.upload.path}")
    private String uploadPath;

    // 업로드
    @PostMapping("upload")
    @ResponseBody
    public List<String> upload(@RequestParam("uploadFile") List<MultipartFile> uploadFiles) throws IOException {
        String rootPath = uploadPath + getPath();
        log.info(rootPath);
        log.info("upload files: {}", uploadFiles);

        List<String> uuids = new ArrayList<>();

        // 모든 파일의 정보를 가져온다..
        File file = new File(rootPath);
        if(!file.exists()){
            file.mkdirs();
        }

        for(int i = 0; i < uploadFiles.size(); i++){
            // uuids를 생성
            uuids.add(UUID.randomUUID().toString());
            uploadFiles.get(i).transferTo(new File(rootPath + uuids.get(i) + "_" + uploadFiles.get(i).getOriginalFilename()));

            // 썸네일 생성
            if(uploadFiles.get(i).getContentType().startsWith("image")){
                FileOutputStream out = new FileOutputStream(new File(rootPath +"t_" + uuids.get(i) + "_" + uploadFiles.get(i).getOriginalFilename()));
                Thumbnailator.createThumbnail(uploadFiles.get(i).getInputStream(), out, 100, 100);
                out.close();
            }
        }

        log.info("upload path: {}", uuids);
        return uuids;
    }

    private String getPath(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
    }

    // 로드
    @GetMapping("display")
    @ResponseBody
    public byte[] display(String fileName) throws IOException {
        log.info("Requested file: {}", fileName);
        log.info("Upload path: {}", uploadPath);
        
        // fileName이 전체 경로를 포함하는 경우 (예: 2024/12/20/uuid_filename.jpg)
        File file;
        if (fileName.contains("/")) {
            // 전체 경로로 파일 찾기
            file = new File(uploadPath + fileName);
        } else {
            // 파일명만 있는 경우 uploadPath에서 직접 찾기
            file = new File(uploadPath, fileName);
        }
        
        log.info("Full file path: {}", file.getAbsolutePath());
        log.info("File exists: {}", file.exists());
        
        if (!file.exists()) {
            log.error("File not found: {}", file.getAbsolutePath());
            throw new IOException("File not found: " + fileName);
        }
        
        return FileCopyUtils.copyToByteArray(file);
    }
}

