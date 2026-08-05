package com.swxt.manager.entity;

import lombok.Data;

/**
 * 商品分类实体，对应数据库表 category
 *
 * <p>支持父子分类层级：parentId=0 表示顶级分类。</p>
 */
@Data
public class Category {

    /** 分类 ID（自增主键） */
    private Long id;

    /** 分类名称，如：电器、玩具、零食 */
    private String name;

    /** 父分类 ID，0 表示顶级分类 */
    private Long parentId;

    /** 排序号，数值越小越靠前 */
    private Integer sortOrder;
}
