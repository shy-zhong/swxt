package com.swxt.manager.mysql;

import com.swxt.manager.entity.SystemConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface SystemConfigMapper {

    /**
     * 查询全部配置项
     */
    @Select("SELECT * FROM system_config ORDER BY id ASC")
    List<SystemConfig> listAll();

    /**
     * 按 ID 查询配置项
     */
    @Select("SELECT * FROM system_config WHERE id = #{id}")
    SystemConfig getById(Long id);

    /**
     * 按配置键查询配置项
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig getByKey(String configKey);

    /**
     * 按 ID 更新配置项（键、值、描述）
     */
    @Update("UPDATE system_config SET config_key = #{configKey}, config_value = #{configValue}, " +
            "description = #{description} WHERE id = #{id}")
    int update(SystemConfig config);

    /**
     * 按配置键仅更新配置值
     */
    @Update("UPDATE system_config SET config_value = #{configValue} WHERE config_key = #{configKey}")
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
