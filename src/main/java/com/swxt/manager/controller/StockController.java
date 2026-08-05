package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.stock.StockOperateRequest;
import com.swxt.manager.entity.StockRecord;
import com.swxt.manager.service.StockService;
import com.swxt.manager.service.SystemLogService;
import com.swxt.manager.Utils.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出入库控制器：入库、出库与记录查询
 */
@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final SystemLogService logService;

    /**
     * 构造出入库控制器：注入出入库服务与日志服务
     */
    public StockController(StockService stockService, SystemLogService logService) {
        this.stockService = stockService;
        this.logService = logService;
    }

    /**
     * 入库
     */
    @PostMapping("/in")
    @PreAuthorize("hasRole('OPERATOR')")
    public Result<Void> stockIn(@RequestBody StockOperateRequest request) {
        String operator = SecurityUtil.getUsername();
        boolean success = stockService.stockIn(request.getProductId(), request.getQuantity(), operator, request.getRemark());
        if (success) {
            logService.record(Core.ActionType.UPDATE, Core.TargetType.STOCK, request.getProductId(),
                    Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.UPDATE, Core.TargetType.STOCK, request.getProductId(), Core.LogResult.FAIL);
        return Result.error(404, "商品不存在");
    }

    /**
     * 出库
     */
    @PostMapping("/out")
    @PreAuthorize("hasRole('OPERATOR')")
    public Result<Void> stockOut(@RequestBody StockOperateRequest request) {
        String operator = SecurityUtil.getUsername();
        boolean success = stockService.stockOut(request.getProductId(), request.getQuantity(), operator, request.getRemark());
        if (success) {
            logService.record(Core.ActionType.UPDATE, Core.TargetType.STOCK, request.getProductId(),
                    Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.UPDATE, Core.TargetType.STOCK, request.getProductId(), Core.LogResult.FAIL);
        return Result.error(400, "出库失败：商品不存在或库存不足");
    }

    /**
     * 查询全部出入库记录
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('OPERATOR')")
    public Result<List<StockRecord>> listRecords() {
        return Result.success(stockService.listAll());
    }

    /**
     * 按商品 ID 查询出入库记录
     */
    @GetMapping("/records/{productId}")
    @PreAuthorize("hasRole('OPERATOR')")
    public Result<List<StockRecord>> listByProduct(@PathVariable Long productId) {
        return Result.success(stockService.listByProductId(productId));
    }
}
