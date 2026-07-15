package com.liaogang.famou.km.role.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.role.model.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper（T205）。
 *
 * <p>KmApplication 已有 @MapperScan("com.liaogang.famou.km.**.repository") 扫描本包
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
}
