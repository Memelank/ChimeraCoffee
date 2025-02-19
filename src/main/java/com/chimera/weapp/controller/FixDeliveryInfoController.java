package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.annotation.RolesAllow;
import com.chimera.weapp.entity.FixDeliveryInfo;
import com.chimera.weapp.enums.RoleEnum;
import com.chimera.weapp.repository.FixDeliveryInfoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/fixDeliveryInfo")
public class FixDeliveryInfoController {
    @Autowired
    private FixDeliveryInfoRepository repository;

//    @GetMapping
//    @Operation(summary = "获取所有定时达信息，用于定时达下单")
//    public ResponseEntity<List<FixDeliveryInfo>> getAllFixDeliveryInfos() {
//        return ResponseEntity.ok(repository.findAll());
//    }

    @PutMapping
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<FixDeliveryInfo> updateFixDeliveryInfo(@RequestBody FixDeliveryInfo entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @GetMapping
    @Operation(summary = "获取所有定时达信息，用于定时达下单")
    public ResponseEntity<List<FixDeliveryInfo>> getAllFixDeliveryInfos() {
        List<FixDeliveryInfo> allFixDeliveryInfos = repository.findAll();

        // 获取当前时间和明天的时间
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));  // 可以根据需要调整时区
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);

//        for (FixDeliveryInfo info : allFixDeliveryInfos) {
//            // 判断今天和明天是否为周末
////            System.out.print(info);
//            boolean isTodayWeekend = isWeekend(today);
//            boolean isTomorrowWeekend = isWeekend(tomorrow);
//
//            // 设置times_today
//            if (isTodayWeekend) {
//                info.setTimes_today(info.getTimes_weekend());
//            } else {
//                info.setTimes_today(info.getTimes_work());
//            }
//
//            // 设置times_tomor
//            if (isTomorrowWeekend) {
//                info.setTimes_tomor(info.getTimes_weekend());
//            } else {
//                info.setTimes_tomor(info.getTimes_work());
//            }
//        }

        return ResponseEntity.ok(allFixDeliveryInfos);
    }

    // 判断是否是周末
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    @PostMapping
    @LoginRequired
    public ResponseEntity<FixDeliveryInfo> createFixDeliveryInfo(@RequestBody FixDeliveryInfo entity) {
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    @LoginRequired
    @RolesAllow(RoleEnum.ADMIN)
    public ResponseEntity<Void> deleteFixDeliveryInfo(@PathVariable String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 返回400错误，表示无效的ObjectId
        }

        if (!repository.existsById(objectId)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(objectId);
        return ResponseEntity.noContent().build();
    }

}

