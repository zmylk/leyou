package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid) {
        //空白匹配，给值得限定
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        if (CollectionUtils.isEmpty(list))
        {
            throw new LyException(ExceptionEnumm.CATEGORY_NOT_FOND);
        }
        return list;
    }

    /**
     * 根据id查询商品分类的id
     * @param ids
     * @return
     */
    public List<Category> queryByIds(List<Long> ids) {
        final List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnumm.CATEGORY_NOT_FOND);
        }
        return list;
    }
}
