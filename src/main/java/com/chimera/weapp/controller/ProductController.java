package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.Activity;
import com.chimera.weapp.entity.Product;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
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

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetStockedFlag() {
        log.info("开始重置needStockAndRestrictBuy=true的商品的stocked为false");
        // 查询所有需要重置的商品
        List<Product> productsToReset = repository.findByNeedStockWithRestrictBuyTrue();

        if (!productsToReset.isEmpty()) {
            for (Product product : productsToReset) {
                product.setStocked(false);
            }
            // 保存更新后的商品
            repository.saveAll(productsToReset);
            log.info("成功重置{}个商品的stocked字段", productsToReset.size());
        } else {
            log.info("没有需要重置的商品");
        }
    }

    @PostMapping("/replenish")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    @Operation(summary = "补货接口，更新商品库存信息")
    public ResponseEntity<String> replenishProduct(
            @RequestParam String productId,
            @RequestParam int replenishQuantity) {
        try {
            // 验证 productId 是否为有效的 ObjectId
            ObjectId productObjectId;
            try {
                productObjectId = new ObjectId(productId);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid product ID format.");
            }

            // 获取对应的 Product
            Product product = repository.findById(productObjectId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
            }

            // 检查补货数量是否合理
            if (replenishQuantity < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Replenish quantity must be at least 1.");
            }

            // 更新库存信息
            product.setStocked(true);
            int newStock = replenishQuantity - product.getPresaleNum();

            // 确保 stock 不为负数
            if (newStock < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Replenish quantity cannot be less than presale number.");
            }

            product.setStock(newStock);
            product.setPresaleNum(0);

            // 保存更新后的 Product
            repository.save(product);

            log.info("Product with ID {} has been replenished. New stock: {}, presaleNum: {}",
                    product.getId(), product.getStock(), product.getPresaleNum());

            return ResponseEntity.ok("Product replenished successfully.");

        } catch (Exception e) {
            log.error("Error during product replenishment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during replenishment.");
        }
    }



    @GetMapping
    @Operation(summary = "获得所有Products，用于菜单展示")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(repository.findAllByDeleteIsAndStatusIs(0, 1));
    }


    @GetMapping("/shop")
    public ResponseEntity<List<Product>> getAllProductsShop() {
        return ResponseEntity.ok(repository.findAllByDeleteIs(0));
    }

    private static final long TARGET_SIZE = 50 * 1024; // 50KB
    private static final double MIN_QUALITY = 0.1;
    private static final double MAX_QUALITY = 1.0;
    private static final double QUALITY_STEP = 0.05;

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

                // 使用 Thumbnailator 创建缩略图，迭代调整质量以接近50KB
                double quality = MAX_QUALITY;
                byte[] thumbnailBytes = null;

                while (quality >= MIN_QUALITY) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Thumbnails.of(destinationFile)
                            .size(800, 800) // 设置较大的尺寸，具体根据需要调整
                            .outputFormat("jpg")
                            .outputQuality(quality)
                            .toOutputStream(baos);

                    thumbnailBytes = baos.toByteArray();
                    long size = thumbnailBytes.length;

                    if (size <= TARGET_SIZE || quality == MIN_QUALITY) {
                        break;
                    }

                    quality -= QUALITY_STEP;
                }

                // 将最终的缩略图写入文件
                if (thumbnailBytes != null) {
                    java.nio.file.Files.write(thumbnailFile.toPath(), thumbnailBytes);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate thumbnail.");
                }

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

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> updateProduct(@Valid  @RequestBody Product entity) {
        // Save the updated product information
        String imgURL = entity.getImgURL();
        entity.setImgURL(url + "product/" + imgURL);
        entity.setImgURL_small(url + "product/" + getThumbnailFilename(imgURL));
        return ResponseEntity.ok(repository.save(entity));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product entity) {
        // Save the product information to the database
        String imgURL = entity.getImgURL();
        entity.setImgURL(url + "product/" + imgURL);
        entity.setImgURL_small(url + "product/" + getThumbnailFilename(imgURL));
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
