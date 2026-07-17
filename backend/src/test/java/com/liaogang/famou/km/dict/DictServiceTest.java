package com.liaogang.famou.km.dict;

import com.liaogang.famou.km.dict.model.DictEntity;
import com.liaogang.famou.km.dict.model.UnitEntity;
import com.liaogang.famou.km.dict.repository.DictMapper;
import com.liaogang.famou.km.dict.repository.UnitMapper;
import com.liaogang.famou.km.dict.service.DictService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T310 U9 字典管理后端测试 - 6 字典 + 9 预制量纲 + 软/硬删除
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DictServiceTest {

    @Mock private DictMapper dictMapper;
    @Mock private UnitMapper unitMapper;
    @Mock private com.liaogang.famou.km.ko.repository.KoMapper koMapper;

    @InjectMocks private DictService dictService;

    private List<DictEntity> dictStore;
    private List<UnitEntity> unitStore;

    @BeforeEach
    void setUp() {
        dictStore = new ArrayList<>();
        // 6 字典 (类型介绍 / 效力分级 / 权威分级 / 类型分组 / 知识对象概念 / 量纲配置)
        String[] types = {"类型介绍", "效力分级", "权威分级", "类型分组", "知识对象概念", "量纲配置"};
        for (int i = 0; i < types.length; i++) {
            DictEntity d = new DictEntity();
            d.setId((long) (i + 1));
            d.setDictType(types[i]);
            d.setCode("CODE_" + i);
            d.setName("项 " + i);
            d.setDisabled(0);
            dictStore.add(d);
        }
        unitStore = new ArrayList<>();
        // 9 预制量纲
        String[] units = {"PERCENT", "YUAN_TEU", "HOUR", "PIECE", "CRANE_PER_HOUR", "ROW", "PERSON_PER_SHIFT", "MACHINE_PER_SHIFT", "BOX_PER_HOUR"};
        for (String u : units) {
            UnitEntity e = new UnitEntity();
            e.setCode(u);
            e.setName(u);
            unitStore.add(e);
        }
        when(dictMapper.selectList(null)).thenAnswer(inv -> dictStore);
        when(dictMapper.findAllActive()).thenAnswer(inv -> dictStore.stream()
                .filter(d -> d.getDisabled() != null && d.getDisabled() == 0)
                .collect(java.util.stream.Collectors.toList()));
        when(dictMapper.findByDictType(any())).thenAnswer(inv -> {
            String type = inv.getArgument(0);
            return dictStore.stream().filter(d -> type.equals(d.getDictType()))
                    .collect(java.util.stream.Collectors.toList());
        });
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
        when(unitMapper.selectList((com.baomidou.mybatisplus.core.conditions.Wrapper<UnitEntity>) null))
                .thenAnswer(inv -> unitStore);
        when(unitMapper.countAll()).thenAnswer(inv -> unitStore.size());
    }

    @Test
    @DisplayName("listAllActive: 6 字典 active 全部返回")
    void listAllActive_6DictsActive() {
        List<DictEntity> list = dictService.listAllActive();
        assertEquals(6, list.size());
    }

    @Test
    @DisplayName("listByDictType: 按 type 过滤")
    void listByDictType_filtered() {
        List<DictEntity> list = dictService.listByDictType("效力分级");
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("softDelete: 标记 disabled=1, 仍可恢复")
    void softDelete_marksDisabled() {
        boolean ok = dictService.softDeleteDict(1L);
        assertTrue(ok);
        verify(dictMapper, times(1)).softDelete(1L);
    }

    @Test
    @DisplayName("undelete: 恢复 disabled=0")
    void undelete_restoresActive() {
        dictService.softDeleteDict(1L);
        boolean ok = dictService.undeleteDict(1L);
        assertTrue(ok);
        verify(dictMapper, times(1)).undelete(1L);
    }

    @Test
    @DisplayName("Edge case: hardDelete 引用完整性校验 - 0 引用时可删")
    void hardDelete_noReference_succeeds() {
        boolean ok = dictService.hardDeleteDict(1L);
        assertTrue(ok, "0 引用时硬删除应成功");
    }

    @Test
    @DisplayName("listAllUnits: 9 预制量纲")
    void listAllUnits_9Presets() {
        List<UnitEntity> units = dictService.listAllUnits();
        assertEquals(9, units.size());
    }

    @Test
    @DisplayName("DefaultDictLoader 部署门禁: 9 量纲缺失即抛异常 (T310 部署门禁)")
    void deployGate_unitMissing_throwsException() {
        // 模拟 9 量纲缺失 (V9009 未执行)
        unitStore.clear();
        when(unitMapper.countAll()).thenReturn(0);
        // 直接调 DefaultDictLoader.run() 验证
        com.liaogang.famou.km.dict.service.DefaultDictLoader loader =
                new com.liaogang.famou.km.dict.service.DefaultDictLoader(unitMapper);
        assertThrows(IllegalStateException.class, () ->
                loader.run(new org.springframework.boot.DefaultApplicationArguments(new String[]{})));
    }

    @Test
    @DisplayName("DefaultDictLoader 部署门禁: 9 量纲齐备时通过")
    void deployGate_unitComplete_passes() {
        // 9 量纲齐备 (默认 setUp)
        com.liaogang.famou.km.dict.service.DefaultDictLoader loader =
                new com.liaogang.famou.km.dict.service.DefaultDictLoader(unitMapper);
        loader.run(new org.springframework.boot.DefaultApplicationArguments(new String[]{}));
        // 无异常 = 通过
    }
}
