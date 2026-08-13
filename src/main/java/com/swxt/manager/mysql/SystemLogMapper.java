package com.swxt.manager.mysql;

import com.swxt.manager.entity.SystemLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemLogMapper {

    @Insert("INSERT INTO system_log(username, user_id, action_type, target_type, target_id, result, ip) " +
            "VALUES(#{username}, #{userId}, #{actionType}, #{targetType}, #{targetId}, #{result}, #{ip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemLog log);

    @Select("<script>" +
            "SELECT * FROM system_log" +
            "<where>" +
            "  <if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if>" +
            "  <if test='actionType != null and actionType != \"\"'>AND action_type = #{actionType}</if>" +
            "  <if test='targetType != null and targetType != \"\"'>AND target_type = #{targetType}</if>" +
            "  <if test='result != null and result != \"\"'>AND result = #{result}</if>" +
            "</where>" +
            " ORDER BY created_at DESC, id ASC LIMIT #{offset}, #{size}" +
            "</script>")
    List<SystemLog> searchLogs(@Param("username") String username,
                               @Param("actionType") String actionType,
                               @Param("targetType") String targetType,
                               @Param("result") String result,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM system_log" +
            "<where>" +
            "  <if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if>" +
            "  <if test='actionType != null and actionType != \"\"'>AND action_type = #{actionType}</if>" +
            "  <if test='targetType != null and targetType != \"\"'>AND target_type = #{targetType}</if>" +
            "  <if test='result != null and result != \"\"'>AND result = #{result}</if>" +
            "</where>" +
            "</script>")
    long countLogs(@Param("username") String username,
                   @Param("actionType") String actionType,
                   @Param("targetType") String targetType,
                   @Param("result") String result);

    @Delete("TRUNCATE TABLE system_log")
    int deleteAll();
}
