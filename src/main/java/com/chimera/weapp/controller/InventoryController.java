package com.chimera.weapp.controller;

import com.chimera.weapp.dto.CheckInventoryRequest;
import com.chimera.weapp.dto.InboundRequest;
import com.chimera.weapp.dto.TimePeriodRequest;
import com.chimera.weapp.entity.Inventory;
import com.chimera.weapp.entity.OperationRecord;
import com.chimera.weapp.repository.InventoryRepository;
import com.chimera.weapp.repository.OperationRecordRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OperationRecordRepository operationRecordRepository;

    // 创建新库存物品
    @PostMapping("/create")
    public ResponseEntity<?> createInventory(@RequestBody Inventory inventory) {
        try {
            // 设置默认值
            inventory.setId(new ObjectId());
            inventory.setDeleted(false);
            // 保存库存物品
            Inventory created = inventoryRepository.save(inventory);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建库存物品失败: " + e.getMessage());
        }
    }

    @PostMapping("/inbound")
    public ResponseEntity<?> inbound(@Validated @RequestBody InboundRequest request) {
        try {
            String inventoryIdStr = request.getInventoryId();
            int amount = request.getAmount();

            ObjectId inventoryId = new ObjectId(inventoryIdStr);
            Optional<Inventory> optionalInventory = inventoryRepository.findByIdAndDeletedFalse(inventoryId);
            if (!optionalInventory.isPresent()) {
                return ResponseEntity.badRequest().body("库存物品未找到");
            }

            Inventory inventory = optionalInventory.get();
            // 更新剩余量
            inventory.setRemain(inventory.getRemain() + amount);
            inventoryRepository.save(inventory);

            // 记录操作
            OperationRecord record = OperationRecord.builder()
                    .id(new ObjectId())
                    .inventoryId(inventoryId)
                    .operationType("INBOUND")
                    .amount(amount)
                    .timestamp(new Date())
                    .build();
            operationRecordRepository.save(record);

            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的库存物品ID格式");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("入库操作失败: " + e.getMessage());
        }
    }

    // 清查操作
    @PostMapping("/check")
    public ResponseEntity<?> checkInventory(@Validated @RequestBody CheckInventoryRequest request) {
        try {
            String inventoryIdStr = request.getInventoryId();
            int checkedAmount = request.getCheckedAmount();

            ObjectId inventoryId = new ObjectId(inventoryIdStr);
            Optional<Inventory> optionalInventory = inventoryRepository.findByIdAndDeletedFalse(inventoryId);
            if (!optionalInventory.isPresent()) {
                return ResponseEntity.badRequest().body("库存物品未找到");
            }

            Inventory inventory = optionalInventory.get();
            int originalRemain = inventory.getRemain();
            int consumption = originalRemain - checkedAmount;

            if (consumption < 0) {
                return ResponseEntity.badRequest().body("清查数量不能大于原有剩余量");
            }

            // 更新剩余量
            inventory.setRemain(checkedAmount);
            inventoryRepository.save(inventory);

            // 记录操作
            OperationRecord record = OperationRecord.builder()
                    .id(new ObjectId())
                    .inventoryId(inventoryId)
                    .operationType("CHECK")
                    .amount(consumption)
                    .timestamp(new Date())
                    .build();
            operationRecordRepository.save(record);

            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的库存物品ID格式");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("清查操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/records/time-period")
    public ResponseEntity<?> getInventoryRecordsByTimePeriod(@Validated @RequestBody TimePeriodRequest request) {
        try {
            Date startTime = request.getStartTime();
            Date endTime = request.getEndTime();

            System.out.println("Start Time: " + startTime);
            System.out.println("End Time: " + endTime);

            // 获取指定时间范围内的操作记录
            List<OperationRecord> records = operationRecordRepository.findByTimestampBetween(startTime, endTime);
            System.out.println("Operation Records: " + records);

            // 获取未删除的库存项并映射ID到名称
            List<Inventory> inventories = inventoryRepository.findAll()
                    .stream()
                    .filter(inv -> !inv.isDeleted())
                    .collect(Collectors.toList());

            Map<ObjectId, String> inventoryIdToName = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getId, Inventory::getName));

            // 生成选中时间段内的所有日期字符串（格式：yyyy-MM-dd）
            List<String> dateKeys = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);
            while (!cal.after(endCal)) {
                String dateKey = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                dateKeys.add(dateKey);
                cal.add(Calendar.DATE, 1);
            }

            int numDays = dateKeys.size();

            // 初始化最终结果Map：键为库存名称，值为包含两个List的Map
            Map<String, Map<String, List<Integer>>> finalResult = new HashMap<>();
            inventories.forEach(inv -> {
                Map<String, List<Integer>> listsMap = new HashMap<>();
                // 初始化入库和查清List，长度为numDays + 1，初始值为0
                List<Integer> inboundList = new ArrayList<>(Collections.nCopies(numDays + 1, 0));
                List<Integer> checkList = new ArrayList<>(Collections.nCopies(numDays + 1, 0));
                listsMap.put("inbound", inboundList);
                listsMap.put("check", checkList);
                finalResult.put(inv.getName(), listsMap);
            });

            // 处理每条操作记录
            for (OperationRecord rec : records) {
                ObjectId inventoryId = rec.getInventoryId();
                String inventoryName = inventoryIdToName.get(inventoryId);
                if (inventoryName == null) {
                    // 库存项可能已被删除，跳过
                    continue;
                }

                String dateKey = new SimpleDateFormat("yyyy-MM-dd").format(rec.getTimestamp());
                int dayIndex = dateKeys.indexOf(dateKey);
                if (dayIndex == -1) {
                    // 记录的日期不在选中范围内，跳过
                    continue;
                }

                Map<String, List<Integer>> listsMap = finalResult.get(inventoryName);
                if (listsMap == null) {
                    continue;
                }

                // 根据操作类型更新对应的List
                if (rec.getOperationType().equalsIgnoreCase("INBOUND")) {
                    List<Integer> inboundList = listsMap.get("inbound");
                    inboundList.set(dayIndex, inboundList.get(dayIndex) + rec.getAmount());
                } else if (rec.getOperationType().equalsIgnoreCase("CHECK")) {
                    List<Integer> checkList = listsMap.get("check");
                    checkList.set(dayIndex, checkList.get(dayIndex) + rec.getAmount());
                }
                // 如果有其他操作类型，可以在此扩展
            }

            // 计算每个List的总和并设置到最后一个元素
            finalResult.forEach((inventoryName, listsMap) -> {
                List<Integer> inboundList = listsMap.get("inbound");
                List<Integer> checkList = listsMap.get("check");

                // 计算入库总和
                int inboundTotal = inboundList.subList(0, numDays).stream().mapToInt(Integer::intValue).sum();
                inboundList.set(numDays, inboundTotal);

                // 计算查清总和
                int checkTotal = checkList.subList(0, numDays).stream().mapToInt(Integer::intValue).sum();
                checkList.set(numDays, checkTotal);
            });

            System.out.println("Final Result: " + finalResult);

            return ResponseEntity.ok(finalResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询操作记录失败: " + e.getMessage());
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateInventory(@RequestParam String id, @RequestBody Inventory updatedInventory) {
        try {
            ObjectId inventoryId = new ObjectId(id);
            Optional<Inventory> optionalInventory = inventoryRepository.findById(inventoryId);
            if (!optionalInventory.isPresent() || optionalInventory.get().isDeleted()) {
                return ResponseEntity.badRequest().body("库存物品未找到");
            }

            Inventory existingInventory = optionalInventory.get();

            // 更新字段（根据需求更新相应字段）
            if (updatedInventory.getName() != null) {
                existingInventory.setName(updatedInventory.getName());
            }
            if (updatedInventory.getType() != null) {
                existingInventory.setType(updatedInventory.getType());
            }
            if (updatedInventory.getUnit() != null) {
                existingInventory.setUnit(updatedInventory.getUnit());
            }

            // 更新 `deleted` 字段
            existingInventory.setDeleted(updatedInventory.isDeleted());

            // 保存更新后的库存物品
            inventoryRepository.save(existingInventory);

            return ResponseEntity.ok(existingInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的库存物品ID格式");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新库存物品失败: " + e.getMessage());
        }
    }



    // 获取所有库存物品
    @GetMapping("/all")
    public ResponseEntity<?> getAllInventories() {
        try {
            List<Inventory> inventories = inventoryRepository.findAll().stream()
                    .filter(inv -> !inv.isDeleted())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取库存列表失败: " + e.getMessage());
        }
    }

    // 获取某个库存物品的操作记录
    @GetMapping("/{id}/records")
    public ResponseEntity<?> getOperationRecords(@PathVariable String id) {
        try {
            ObjectId inventoryId = new ObjectId(id);
            List<OperationRecord> records = operationRecordRepository.findByInventoryId(inventoryId);
            return ResponseEntity.ok(records);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的库存物品ID格式");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取操作记录失败: " + e.getMessage());
        }
    }

}
