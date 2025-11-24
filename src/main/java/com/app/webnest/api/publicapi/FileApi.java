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

    // 예: C:/upload/
    private String uploadPath;
    // 업로드
    @PostMapping("upload")
    @ResponseBody
    public List<String> upload(@RequestParam("uploadFile") List<MultipartFile> uploadFiles) throws IOException {
        String path = getPath();
        String rootPath = "C:/upload/" + path;
        log.info("파일 업로드 경로: {}", rootPath);

        List<String> uuids = new ArrayList<>();

        // 디렉터리 생성
        File file = new File(rootPath);
        if(!file.exists()){
            file.mkdirs();
            log.info("디렉터리 생성 완료: {}", rootPath);
        }

        for(int i = 0; i < uploadFiles.size(); i++){
//            uuids를 생성
            uuids.add(UUID.randomUUID().toString());
            uploadFiles.get(i).transferTo(new File(rootPath + uuids.get(i) + "_" + uploadFiles.get(i).getOriginalFilename() ));

//            썸네일 생성
            if(uploadFiles.get(i).getContentType().startsWith("image")){
                FileOutputStream out = new FileOutputStream(new File(rootPath +"t_" + uuids.get(i) + "_" + uploadFiles.get(i).getOriginalFilename()));
                Thumbnailator.createThumbnail(uploadFiles.get(i).getInputStream(), out, 100, 100);
                out.close();
            }

        }


        return uuids;
    }

    private String getPath(){return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));}

    // 로드
    @GetMapping("display")
    @ResponseBody
    public byte[] display(@RequestParam String fileName) throws IOException {
        String rootPath = "C:/upload/" ;
        return FileCopyUtils.copyToByteArray(new File(rootPath, fileName));

    }
}