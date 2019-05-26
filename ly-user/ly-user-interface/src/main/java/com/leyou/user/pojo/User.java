package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * 用户对象
 *
 * author 暗氵愧
 * HostName dell
 * Date 2018/12/27 19:21
 */
@Data
@Table(name = "tb_user")
public class User {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotEmpty(message = "用户名不能为空!")
    @Length(min = 4,max = 32,message = "用户必须在4-32位！")
    private String username; //用户名

    @Length(min = 4,max = 32,message = "密码必须在4-32位！")
    @JsonIgnore // 返回json数据时不返回密码
    private String password; // 密码

    @Pattern(regexp = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$",message = "手机号不正确！")
    private String phone; // 电话号码

    private Date created; //创建时间

    @JsonIgnore
    private String salt; //面的盐值
}
