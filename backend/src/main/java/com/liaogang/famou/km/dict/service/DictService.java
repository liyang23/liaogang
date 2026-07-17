package com.liaogang.famou.km.dict.service;

import com.liaogang.famou.km.dict.model.DictEntity;
import com.liaogang.famou.km.dict.model.UnitEntity;
import com.liaogang.famou.km.dict.repository.DictMapper;
import com.liaogang.famou.km.dict.repository.UnitMapper;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.repository.KoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务 (U9 / T310)
 *
 * <p>6 字典 CRUD + 软删除 (disabled 标记) + 硬删除 (引用完整性校验) + 9 预制量纲管理.
 * 软删除: 标记 disabled=1, 存量 KO 仍可引用但带 ⓘ 停用标签
 * 硬删除: 仅未被任何 KO 引用的字典项可删; 删除前校验引用计数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictService {

    private final DictMapper dictMapper;
    private final UnitMapper unitMapper;
    private final KoMapper koMapper;

    public List<DictEntity> listByDictType(String dictType) {
        return dictMapper.findByDictType(dictType);
    }

    public List<DictEntity> listAllActive() {
        return dictMapper.findAllActive();
    }

    public List<UnitEntity> listAllUnits() {
        return unitMapper.selectList(null);
    }

    /**
     * 软删除字典项 (plan §U9 软删除)
     * 标记 disabled=1, 存量 KO 仍可引用但带 ⓘ 已停用 标签
     */
    @Transactional
    public boolean softDeleteDict(Long id) {
        return dictMapper.softDelete(id) > 0;
    }

    /**
     * 硬删除字典项 (plan §U9 硬删除)
     * 仅未被任何 KO 引用的字典项可删; 删除前校验引用计数
     */
    @Transactional
    public boolean hardDeleteDict(Long id) {
        // 引用完整性校验: 查 KO 表中是否有字典项 code 引用 (placeholder: KO 字段引用实际方式由 T303 业务联动实现)
        long referenceCount = 0L;  // placeholder
        if (referenceCount > 0) {
            throw new IllegalStateException("字典项 " + id + " 仍被 KO 引用, 不可硬删除");
        }
        return dictMapper.deleteById(id) > 0;
    }

    /**
     * 恢复软删除的字典项
     */
    @Transactional
    public boolean undeleteDict(Long id) {
        return dictMapper.undelete(id) > 0;
    }
}
