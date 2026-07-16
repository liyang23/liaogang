package com.liaogang.famou.km.ko.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.ko.model.KoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * KO 库主表 Mapper（T201 实现）。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper} 提供 CRUD：
 * <ul>
 *   <li>insert / updateById / selectById / deleteById</li>
 *   <li>selectList(Wrapper) / selectPage(Page, Wrapper) 复杂查询</li>
 *   <li>逻辑删除：{@code is_deleted = 0} 自动过滤（@TableLogic 后续 T202 加）</li>
 * </ul>
 *
 * <p>KmApplication 已有 {@code @MapperScan("com.liaogang.famou.km.**.repository")} 扫描本包
 */
@Mapper
public interface KoMapper extends BaseMapper<KoEntity> {
}
