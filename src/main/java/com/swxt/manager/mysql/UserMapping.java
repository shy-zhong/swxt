package com.swxt.manager.mysql;

import com.swxt.manager.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapping {

    /** 按用户名查询未删除用户（账号密码登录） */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User loginByUsername(String username);

    /** 按用户 ID 查询未删除用户 */
    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User loginById(String id);

    /** 按微信 openid 查询未删除用户（微信扫码登录） */
    @Select("SELECT * FROM user WHERE wechat_openid = #{openid} AND deleted = 0")
    User loginByWechatOpenid(String openid);

    /** 统计指定用户名的未删除用户数（注册重名校验） */
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND deleted = 0")
    int countByUsername(String username);

    /* ===================== 列表与分页 ===================== */

    /** 查询全部未删除用户 */
    @Select("SELECT * FROM user WHERE deleted = 0")
    List<User> listUsers();

    /** 分页查询未删除用户（按 id 升序） */
    @Select("SELECT * FROM user WHERE deleted = 0 ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> listUsersPage(@Param("offset") int offset, @Param("size") int size);

    /** 统计未删除用户总数 */
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countUsers();

    /* ===================== 搜索 ===================== */

    /** 按指定字段模糊搜索未删除用户并分页 */
    @Select("SELECT * FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0 " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> searchUsersPage(@Param("key") String key,
                               @Param("value") String value,
                               @Param("offset") int offset,
                               @Param("size") int size);

    /** 统计按指定字段模糊搜索的未删除用户数 */
    @Select("SELECT COUNT(*) FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0")
    long countSearchUsers(@Param("key") String key, @Param("value") String value);

    /** 按关键词模糊搜索用户名/真实姓名/手机号/邮箱并分页 */
    @Select("SELECT * FROM user WHERE deleted = 0 " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> listUsersKeywordPage(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    /** 统计按关键词模糊搜索的未删除用户数 */
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%'))")
    long countUsersKeyword(@Param("keyword") String keyword);

    /* ===================== 增删改 ===================== */

    /** 新增用户（回填自增主键 id） */
    @Insert("INSERT INTO user(username, password, role, real_name, phone, email, wechat_openid, status) " +
            "VALUES(#{username}, #{password}, #{role}, #{realName}, #{phone}, #{email}, #{wechatOpenid}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createNewUser(User user);

    /** 软删除用户（deleted 置 1） */
    @Update("UPDATE user SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int deleteUserById(Long id);

    /** 更新用户基本信息、微信 openid、角色与状态 */
    @Update("UPDATE user SET username = #{username}, real_name = #{realName}, phone = #{phone}, " +
            "email = #{email}, wechat_openid = #{wechatOpenid}, role = #{role}, status = #{status} " +
            "WHERE id = #{id}")
    int updateUser(User user);
}
