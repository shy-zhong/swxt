package com.swxt.manager.service;

import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.statistics.StatisticsVO;
import com.swxt.manager.mysql.StatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    
    private static final String[] PRICE_LABELS = {"0-100", "100-500", "500-1000", "1000-3000", "3000+"};
    
    private static final String[] PRICE_KEYS = {"r1", "r2", "r3", "r4", "r5"};

/**
 * 组装完整的统计面板数据
 */
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();
        vo.setSummary(buildSummary());
        vo.setCategoryStats(buildCategoryStats());
        vo.setPriceRangeStats(buildPriceRangeStats());
        return vo;
    }

/**
 * 汇总指标：商品、用户、库存、价格各维度
 */
    private StatisticsVO.Summary buildSummary() {
        StatisticsVO.Summary s = new StatisticsVO.Summary();
        long totalProducts = statisticsMapper.countProducts();
        long activeProducts = statisticsMapper.countActiveProducts();

        s.setTotalProducts(totalProducts);
        s.setActiveProducts(activeProducts);
        s.setInactiveProducts(totalProducts - activeProducts);

        long totalUsers = statisticsMapper.countUsers();
        long adminUsers = statisticsMapper.countAdminUsers();
        s.setTotalUsers(totalUsers);
        s.setAdminUsers(adminUsers);
        s.setNormalUsers(totalUsers - adminUsers);

        s.setTotalStock(statisticsMapper.sumStock());
        s.setStockValue(round2(statisticsMapper.sumStockValue()));
        s.setAvgPrice(round2(statisticsMapper.avgPrice()));
        s.setMaxPrice(round2(statisticsMapper.maxPrice()));
        s.setMinPrice(round2(statisticsMapper.minPrice()));
        return s;
    }

/**
 * 分类统计：各分类的商品数、库存、平均价与占比
 */
    private List<StatisticsVO.CategoryStat> buildCategoryStats() {
        List<Map<String, Object>> rows = statisticsMapper.categoryAgg();
        long total = statisticsMapper.countProducts();
        List<StatisticsVO.CategoryStat> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            StatisticsVO.CategoryStat cs = new StatisticsVO.CategoryStat();
            cs.setCategoryId(toLong(row.get("category_id")));
            cs.setCategoryName((String) row.get("category_name"));
            long count = toLong(row.get("count"));
            cs.setCount(count);
            cs.setStock(toLong(row.get("stock")));
            cs.setAvgPrice(round2(toDouble(row.get("avg_price"))));
            cs.setPercent(total > 0 ? round2(count * 100.0 / total) : 0);
            list.add(cs);
        }
        return list;
    }

/**
 * 价格区间统计：按固定 5 个区间统计商品数量与占比
 */
    private List<StatisticsVO.PriceRangeStat> buildPriceRangeStats() {
        Map<String, Object> row = statisticsMapper.priceRangeAgg();
        long total = statisticsMapper.countProducts();
        List<StatisticsVO.PriceRangeStat> list = new ArrayList<>();
        for (int i = 0; i < PRICE_KEYS.length; i++) {
            StatisticsVO.PriceRangeStat p = new StatisticsVO.PriceRangeStat();
            p.setLabel(PRICE_LABELS[i]);
            p.setRangeKey(PRICE_KEYS[i]);
            long count = toLong(row.get(PRICE_KEYS[i]));
            p.setCount(count);
            p.setPercent(total > 0 ? round2(count * 100.0 / total) : 0);
            list.add(p);
        }
        return list;
    }

/**
 * 库存流水分页查询
 */
    public PageResult<StatisticsVO.StockRecordItem> stockRecordPage(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<Map<String, Object>> rows = statisticsMapper.stockRecordPage(offset, size);
        long total = statisticsMapper.countStockRecords();
        return new PageResult<>(mapStockRecords(rows), total, page, size);
    }

    public PageResult<StatisticsVO.StockRecordItem> searchStockRecords(String value, int page, int size) {
        if (value == null || value.isEmpty()) return stockRecordPage(page, size);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<Map<String, Object>> rows = statisticsMapper.searchStockRecordsPage(value, offset, size);
        long total = statisticsMapper.countSearchStockRecords(value);
        return new PageResult<>(mapStockRecords(rows), total, page, size);
    }

/**
 * 将库存流水 Map 结果转换为 VO 列表
 */
    private List<StatisticsVO.StockRecordItem> mapStockRecords(List<Map<String, Object>> rows) {
        List<StatisticsVO.StockRecordItem> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            StatisticsVO.StockRecordItem item = new StatisticsVO.StockRecordItem();
            item.setId(toLong(row.get("id")));
            item.setProductId(toLong(row.get("product_id")));
            item.setProductName((String) row.get("product_name"));
            item.setType((String) row.get("type"));
            item.setQuantity(toInt(row.get("quantity")));
            item.setOperator((String) row.get("operator"));
            item.setRemark((String) row.get("remark"));
            item.setCreatedAt((String) row.get("created_at"));
            list.add(item);
        }
        return list;
    }

/**
 * 数值保留两位小数
 */
    private static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

/**
 * Long 转换
 */
    private static long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

/**
 * int 转换
 */
    private static int toInt(Object o) {
        return (int) toLong(o);
    }

/**
 * double 转换
 */
    private static double toDouble(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
