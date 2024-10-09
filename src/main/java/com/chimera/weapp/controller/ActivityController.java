package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Activity;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ActivityRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Value("${app.url}")
    private String url;

    @Autowired
    private ActivityRepository repository;

    /**
     * 每天凌晨1点检查活动是否已过期，过期则下架（status设为0）
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    @Transactional
    public void checkAndUpdateExpiredActivities() {
        Date now = new Date();
        List<Activity> expiredActivities = repository.findAllByStatusIsAndEndTimeBefore(1, now);

        for (Activity activity : expiredActivities) {
            activity.setStatus(0); // 将状态改为0，表示下架
            repository.save(activity); // 更新数据库
        }
    }

    @GetMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    @Operation(summary = "获得所有活动")
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

    @GetMapping("/wx")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    @Operation(summary = "获取所有小程序展示活动")
    public ResponseEntity<List<Activity>> getAllActivitiesWX() {
        return ResponseEntity.ok(repository.findAllByDeleteIsAndStatusIs(0, 1));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Activity> updateActivity(@RequestBody Activity entity) {
        entity.setImgURL(url + "activity/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = imageFile.getOriginalFilename();
                File destinationDir = new File(uploadDirectory + "activity/");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // 创建目录
                }
                File destinationFile = new File(destinationDir, filename);
                imageFile.transferTo(destinationFile);
                return ResponseEntity.ok(filename); // Return only filename
            }
            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
        } catch (MultipartException e) {
            // 打印详细错误信息
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse multipart request");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save file");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Activity> createEntity(@RequestBody Activity entity) {
        entity.setImgURL(url + "activity/" + entity.getImgURL());
        return ResponseEntity.ok(repository.save(entity));
    }

//    @PostMapping(consumes = {"multipart/form-data"})
//    @LoginRequired
//    @RolesAllow(RoleEnum.ADMIN)
//    public ResponseEntity<Activity> createActivity(
//            @RequestPart("activity") Activity entity,
//            @RequestPart("image") MultipartFile imageFile) throws IOException {
//
//        // 上传文件到服务器
//        if (!imageFile.isEmpty()) {
//            String filename = imageFile.getOriginalFilename();
//            File destinationFile = new File(uploadDirectory + "activity/" + filename);
//            imageFile.transferTo(destinationFile);
//
//            // 将上传后的文件路径或URL存储到imgURL中
//            entity.setImgURL(url + "activity/" + filename);  // 可以根据实际情况调整URL前缀
//        }
//
//        // 保存产品信息到数据库
//        return ResponseEntity.ok(repository.save(entity));
//    }

}
