package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //查询
        List<SpecGroup> groups = groupMapper.select(specGroup);
        if (groups == null)
        {
            throw new LyException(ExceptionEnumm.SPEC_GROUP_NOT_FOND);
        }
        //返回
        return groups;
    }

    public List<SpecParam> queryParamsByList(Long gid,Long cid,Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> params = paramMapper.select(param);
        if (params == null)
        {
            throw new LyException(ExceptionEnumm.SPEC_PRAM_NOT_FOND);
        }
        //返回
        return params;
    }

    public List<SpecGroup> queryListBycid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查询组内参数
        List<SpecParam> params = queryParamsByList(null,cid,null);
        //先把规格参数变成map，map的key是规格参数的组id，value是规格参数的值
        Map<Long,List<SpecParam>>   map = new HashMap<>();
        for (SpecParam param : params) {
            if (!map.containsKey(param.getGroupId()))
            {
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //填充param到group中
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        return specGroups;
    }
}
