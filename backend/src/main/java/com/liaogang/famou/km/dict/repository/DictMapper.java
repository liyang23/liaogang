package com.liaogang.famou.km.dict.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.dict.model.DictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 字典 MyBatis-Plus Mapper (U9 / T310)
 */
@Mapper
public interface DictMapper extends BaseMapper<DictEntity> {

    @Select("SELECT * FROM km_dict WHERE dict_type = #{dictType} ORDER BY sort_order ASC")
    List<DictEntity> findByDictType(@Param("dictType") String dictType);

    @Select("SELECT * FROM km_dict WHERE disabled = 0 ORDER BY dict_type ASC, sort_order ASC")
    List<DictEntity> findAllActive();

    @Update("UPDATE km_dict SET disabled = 1, updated_at = NOW() WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    @Update("UPDATE km_dict SET disabled = 0, updated_at = NOW() WHERE id = #{id}")
    int undelete(@Param("id") Long id);
}
