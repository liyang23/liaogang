package com.liaogang.famou.km.dict.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.dict.model.UnitEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 量纲 MyBatis-Plus Mapper (U9 / T310)
 */
@Mapper
public interface UnitMapper extends BaseMapper<UnitEntity> {

    default int countAll() {
        return selectList(null).size();
    }
}
