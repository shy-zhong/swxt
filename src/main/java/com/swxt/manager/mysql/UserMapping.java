package com.swxt.manager.mysql;

import com.swxt.manager.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface UserMapping {

/**
 * 按用户名查询用户（登录用）
 */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User loginByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User loginById(String id);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND deleted = 0")
    int countByUsername(String username);

    @Select("SELECT * FROM user WHERE deleted = 0")
    List<User> listUsers();

    @Select("SELECT * FROM user WHERE deleted = 0 ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> listUsersPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 按指定字段模糊搜索用户（字段名动态拼接，值使用 LIKE 模糊匹配）
     */
    @Select("SELECT * FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0 " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> searchUsersPage(@Param("key") String key,
                               @Param("value") String value,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("SELECT COUNT(*) FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0")
    long countSearchUsers(@Param("key") String key, @Param("value") String value);

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countUsers();

    /**
     * 按关键词模糊搜索用户（用户名/真实姓名/手机号/邮箱）
     */
    @Select("SELECT * FROM user WHERE deleted = 0 " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> listUsersKeywordPage(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%'))")
    long countUsersKeyword(@Param("keyword") String keyword);

/**
 * 新增用户（密码为 BCrypt 加密后的密文）
 */
    @Insert("INSERT INTO user(username, password, role, real_name, phone, email, wechat_openid, status) " +
            "VALUES(#{username}, #{password}, #{role}, #{realName}, #{phone}, #{email}, #{wechatOpenid}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createNewUser(User user);

    @Update("UPDATE user SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int deleteUserById(Long id);


/**
 * 更新用户基本信息、角色与状态（不修改密码）
 */
    @Update("UPDATE user SET username = #{username}, real_name = #{realName}, phone = #{phone}, " +
            "email = #{email}, wechat_openid = #{wechatOpenid}, role = #{role}, status = #{status} " +
            "WHERE id = #{id}")
    int updateUser(User user);
}
