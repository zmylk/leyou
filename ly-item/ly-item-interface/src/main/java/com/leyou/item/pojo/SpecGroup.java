package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Table(name = "tb_spec_group")
@Data
public class SpecGroup {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long cid;

    private String name;

    // getter和setter省略
    @Transient
    private List<SpecParam> params;
}