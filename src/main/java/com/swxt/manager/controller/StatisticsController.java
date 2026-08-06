package com.swxt.manager.controller;

import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.statistics.StatisticsVO;
import com.swxt.manager.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 构造统计控制器：注入统计服务
     */
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取统计面板数据
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsVO> statistics() {
        return Result.success(statisticsService.getStatistics());
    }

    /**
     * 库存流水分页查询
     */
    @GetMapping("/stock-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public Result<PageResult<StatisticsVO.StockRecordItem>> stockRecords(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(statisticsService.stockRecordPage(page, size));
    }
    /**
     * 库存流水条件查询
     */
    @GetMapping("/stock-records/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public Result<PageResult<StatisticsVO.StockRecordItem>> searchStockRecords(
            @RequestParam(value = "value") String value,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(statisticsService.searchStockRecords(value, page, size));
    }
}
