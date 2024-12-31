package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Value("${app.url}")
    private String url;

    @Autowired
    private ProductRepository repository;

    @GetMapping
    @Operation(summary = "获得所有Products，用于菜单展示")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(repository.findAllByDeleteIsAndStatusIs(0, 1));
    }


    @GetMapping("/shop")
    public ResponseEntity<List<Product>> getAllProductsShop() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

//    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @LoginRequired
//    @RolesAllow(RoleEnum.ADMIN)
//    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile) {
//        try {
//            if (imageFile != null && !imageFile.isEmpty()) {
//                String filename = imageFile.getOriginalFilename();
//                File destinationDir = new File(uploadDirectory + "product/");
//                if (!destinationDir.exists()) {
//                    destinationDir.mkdirs(); // 创建目录
//                }
//                File destinationFile = new File(destinationDir, filename);
//                imageFile.transferTo(destinationFile);
//                return ResponseEntity.ok(filename); // Return only filename
//            }
//            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
//        } catch (MultipartException e) {
//            // 打印详细错误信息
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse multipart request");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save file");
//        }
//    }

    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                if (originalFilename == null) {
                    return ResponseEntity.badRequest().body("Invalid file name.");
                }

                // 保存原始图片
                File destinationDir = new File(uploadDirectory + "product/");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // 创建目录
                }
                File destinationFile = new File(destinationDir, originalFilename);
                imageFile.transferTo(destinationFile);

                // 创建缩略图
                String thumbnailFilename = getThumbnailFilename(originalFilename);
                File thumbnailFile = new File(destinationDir, thumbnailFilename);

                // 使用 Thumbnailator 创建缩略图，尝试压缩到接近50KB
                // 这里设置尺寸为原图的25%，可以根据需要调整
                Thumbnails.of(destinationFile)
                        .size(200, 200) // 设置缩略图的宽度和高度（根据实际需要调整）
                        .outputFormat("jpg")
                        .outputQuality(calculateQuality(destinationFile, 50 * 1024)) // 目标大小为50KB
                        .toFile(thumbnailFile);

                return ResponseEntity.ok(originalFilename); // 返回原始文件名
            }
            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
        } catch (MultipartException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse multipart request");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save file");
        }
    }

    /**
     * 生成缩略图文件名
     */
    private String getThumbnailFilename(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1) {
            String name = originalFilename.substring(0, dotIndex);
            String extension = originalFilename.substring(dotIndex);
            return name + "_small" + extension;
        } else {
            return originalFilename + "_small";
        }
    }

    /**
     * 计算输出质量以尽量接近目标文件大小
     * 注意：这只是一个简单的估算，实际结果可能需要多次调整
     */
    private double calculateQuality(File file, long targetSize) throws IOException {
        // 初始质量
        double quality = 1.0;
        long fileSize = file.length();
        // 简单的线性调整质量
        if (fileSize > targetSize) {
            quality = (double) targetSize / fileSize;
            // 限制质量范围
            if (quality < 0.1) {
                quality = 0.1;
            }
        }
        return quality;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> updateProduct(@Valid  @RequestBody Product entity) {
        // Save the updated product information
        entity.setImgURL(url + "product/" + entity.getImgURL());
        entity.setImgURL_small(url + "product/" + getThumbnailFilename(entity.getImgURL()));
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product entity) {
        // Save the product information to the database
        entity.setImgURL(url + "product/" + entity.getImgURL());
        entity.setImgURL_small(url + "product/" + getThumbnailFilename(entity.getImgURL()));
        return ResponseEntity.ok(repository.save(entity));
    }

    // 判断cateId分类是否在使用
    @GetMapping("/existsByCateId")
    public ResponseEntity<Boolean> existsByCateId(@Valid @RequestParam String cateId) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(cateId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean exists = repository.existsByCateIdAndDelete(objectId, 0);
        return ResponseEntity.ok(exists);
    }

}
