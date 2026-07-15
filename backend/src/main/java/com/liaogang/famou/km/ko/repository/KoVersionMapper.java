package com.liaogang.famou.km.ko.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.ko.model.KoVersionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * KO 版本历史 Mapper（T201 实现）。
 *
 * <p>提供版本历史 CRUD + 按 ko_id 查询历史列表
 */
@Mapper
public interface KoVersionMapper extends BaseMapper<KoVersionEntity> {

    /**
     * 查询某 KO 的所有版本（按时间倒序）
     *
     * @param koId KO ID
     * @return 版本历史列表（最新版本在前）
     */
    default java.util.List<KoVersionEntity> selectByKoId(String koId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KoVersionEntity>()
                .eq("ko_id", koId)
                .orderByDesc("created_at")
        );
    }
}
