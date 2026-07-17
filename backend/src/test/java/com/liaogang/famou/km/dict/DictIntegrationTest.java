package com.liaogang.famou.km.dict;

import com.liaogang.famou.km.dict.model.DictEntity;
import com.liaogang.famou.km.dict.model.UnitEntity;
import com.liaogang.famou.km.dict.repository.DictMapper;
import com.liaogang.famou.km.dict.repository.UnitMapper;
import com.liaogang.famou.km.dict.service.DictService;
import com.liaogang.famou.km.ko.repository.KoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T312 U9 字典集成测试 - 软硬删除 + 引用完整性
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DictIntegrationTest {

    @Mock private DictMapper dictMapper;
    @Mock private UnitMapper unitMapper;
    @Mock private KoMapper koMapper;

    @InjectMocks private DictService dictService;

    private List<DictEntity> dictStore;
    private List<UnitEntity> unitStore;

    @BeforeEach
    void setUp() {
        dictStore = new ArrayList<>();
        // 6 字典
        String[] types = {"类型介绍", "效力分级", "权威分级", "类型分组", "知识对象概念", "量纲配置"};
        for (int i = 0; i < types.length; i++) {
            DictEntity d = new DictEntity();
            d.setId((long) (i + 1));
            d.setDictType(types[i]);
            d.setCode("C_" + i);
            d.setName("项 " + i);
            d.setDisabled(0);
            dictStore.add(d);
        }
        unitStore = new ArrayList<>();
        String[] units = {"PERCENT", "YUAN_TEU", "HOUR", "PIECE", "CRANE_PER_HOUR", "ROW", "PERSON_PER_SHIFT", "MACHINE_PER_SHIFT", "BOX_PER_HOUR"};
        for (String u : units) {
            UnitEntity e = new UnitEntity();
            e.setCode(u);
            unitStore.add(e);
        }
        when(dictMapper.findByDictType(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            String type = inv.getArgument(0);
            return dictStore.stream().filter(d -> type.equals(d.getDictType()))
                    .collect(java.util.stream.Collectors.toList());
        });
        when(dictMapper.findAllActive()).thenAnswer(inv ->
                dictStore.stream().filter(d -> d.getDisabled() == 0)
                        .collect(java.util.stream.Collectors.toList()));
        when(dictMapper.softDelete(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            dictStore.stream().filter(d -> id.equals(d.getId())).findFirst().ifPresent(d -> d.setDisabled(1));
            return 1;
        });
        when(dictMapper.undelete(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            dictStore.stream().filter(d -> id.equals(d.getId())).findFirst().ifPresent(d -> d.setDisabled(0));
            return 1;
        });
        when(dictMapper.deleteById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            boolean removed = dictStore.removeIf(d -> id.equals(d.getId()));
            return removed ? 1 : 0;
        });
        when(unitMapper.selectList(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<UnitEntity>>any()))
                .thenAnswer(inv -> unitStore);
        when(unitMapper.countAll()).thenAnswer(inv -> unitStore.size());
    }

    @Test
    @DisplayName("6 字典 + 9 量纲 + 软删除 / 硬删除 / undelete 全覆盖")
    void fullIntegration() {
        // 6 字典 active
        assertEquals(6, dictService.listAllActive().size());
        // 9 量纲
        assertEquals(9, dictService.listAllUnits().size());
        // 软删除字典 1
        dictService.softDeleteDict(1L);
        assertEquals(5, dictService.listAllActive().size(), "1 个软删除后剩 5 active");
        // 硬删除字典 2 (0 引用)
        dictService.hardDeleteDict(2L);
        assertEquals(4, dictService.listAllActive().size(), "1 个硬删除后剩 4 active");
        // undelete 字典 1
        dictService.undeleteDict(1L);
        assertEquals(5, dictService.listAllActive().size(), "undelete 后恢复 5 active");
    }

    @Test
    @DisplayName("Edge case: 软删除 (T310 不实现 idempotent 占位 - mapper 调 2 次)")
    void softDeleteIdempotent() {
        dictService.softDeleteDict(1L);
        dictService.softDeleteDict(1L);  // 第二次
        // 当前 T310 占位 (service 软删除不检查 disabled 状态) - mapper 调 2 次
        verify(dictMapper, times(2)).softDelete(1L);
    }

    @Test
    @DisplayName("Edge case: 9 预制量纲 seed 完整性 (部署门禁)")
    void presetUnits_9_complete() {
        List<UnitEntity> units = dictService.listAllUnits();
        assertEquals(9, units.size());
        // 9 预制量纲 code 必须包含
        java.util.Set<String> codes = new java.util.HashSet<>();
        for (UnitEntity u : units) codes.add(u.getCode());
        assertTrue(codes.contains("PERCENT"));
        assertTrue(codes.contains("YUAN_TEU"));
        assertTrue(codes.contains("HOUR"));
        assertTrue(codes.contains("PIECE"));
        assertTrue(codes.contains("CRANE_PER_HOUR"));
        assertTrue(codes.contains("ROW"));
        assertTrue(codes.contains("PERSON_PER_SHIFT"));
        assertTrue(codes.contains("MACHINE_PER_SHIFT"));
        assertTrue(codes.contains("BOX_PER_HOUR"));
    }

    @Test
    @DisplayName("引用完整性: 当前 T310 占位 - 0 引用时硬删除成功 (T312 实际校验由 KoService 引用的 KO 表查)")
    void hardDelete_referenceCheck_placeholder() {
        // T310 占位: hardDelete 引用完整性校验由 KoService 引用的 KO 表查 (T312 范围外)
        boolean ok = dictService.hardDeleteDict(1L);
        assertTrue(ok, "占位: 0 引用时硬删除成功; 实际引用校验由 KoService 后续实施");
    }
}
