package com.chimera.weapp.controller;

import com.chimera.weapp.entity.Activity;
import com.chimera.weapp.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Autowired
    private ActivityRepository repository;

    @GetMapping
    public ResponseEntity<List<Activity>> getAllEntities() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping
    public ResponseEntity<Activity> updateEntity(@RequestBody Activity entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

//    @PostMapping
//    public ResponseEntity<Activity> createEntity(@RequestBody Activity entity) {
//        return ResponseEntity.ok(repository.save(entity));
//    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Activity> createEntity(
            @RequestPart("activity") Activity entity,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        // 上传文件到服务器
        if (!imageFile.isEmpty()) {
            String filename = imageFile.getOriginalFilename();
            File destinationFile = new File(uploadDirectory + filename);
            imageFile.transferTo(destinationFile);

            // 将上传后的文件路径或URL存储到imgURL中
            entity.setImgURL(filename);  // 可以根据实际情况调整URL前缀
        }

        // 保存产品信息到数据库
        return ResponseEntity.ok(repository.save(entity));
    }

}