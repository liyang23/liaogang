package com.liaogang.famou.km.prompt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.repository.PrmSectionMapper;
import com.liaogang.famou.km.prompt.repository.PrmTemplateMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PRM 模板服务（T208）。
 *
 * <p>启动时自动加载 seed/prm-templates.yaml → 3 预置 PRM 模板 + 17 段
 * <p>幂等：模板已存在跳过；section 已存在跳过
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrmService {

    private final PrmTemplateMapper prmTemplateMapper;
    private final PrmSectionMapper prmSectionMapper;

    /**
     * 启动加载（T208 done_signal）
     */
    @PostConstruct
    public void load() {
        try {
            loadTemplates();
            loadSections();
            log.info("✓ 3 预置 PRM 模板 + 17 段加载完成");
        } catch (Exception e) {
            log.error("✗ PRM 模板加载失败", e);
        }
    }

    @Transactional
    public void loadTemplates() {
        List<Map<String, Object>> templates = readSeed();
        for (Map<String, Object> data : templates) {
            String id = (String) data.get("id");
            Long count = prmTemplateMapper.selectCount(
                new QueryWrapper<PrmTemplateEntity>().eq("id", id)
            );
            if (count > 0) continue;

            PrmTemplateEntity template = PrmTemplateEntity.builder()
                .id(id)
                .name((String) data.get("name"))
                .description((String) data.get("description"))
                .version((String) data.getOrDefault("version", "v1.0.0"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            prmTemplateMapper.insert(template);
            log.info("✓ PRM 模板加载: {}", id);
        }
    }

    @Transactional
    public void loadSections() {
        List<Map<String, Object>> templates = readSeed();
        for (Map<String, Object> data : templates) {
            String templateId = (String) data.get("id");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sections = (List<Map<String, Object>>) data.get("sections");
            if (sections == null) continue;

            for (Map<String, Object> sec : sections) {
                Integer index = (Integer) sec.get("index");
                // 幂等检查
                Long count = prmSectionMapper.selectCount(
                    new QueryWrapper<PrmSectionEntity>()
                        .eq("template_id", templateId)
                        .eq("section_index", index)
                );
                if (count > 0) continue;

                PrmSectionEntity section = PrmSectionEntity.builder()
                    .templateId(templateId)
                    .sectionIndex(index)
                    .title((String) sec.get("title"))
                    .sectionType((String) sec.get("section_type"))
                    .content((String) sec.get("content"))
                    .createdAt(LocalDateTime.now())
                    .build();
                prmSectionMapper.insert(section);
            }
        }
    }

    /**
     * 查询模板（含 Section 列表）
     */
    public PrmTemplateEntity getTemplate(String id) {
        PrmTemplateEntity template = prmTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("PRM 模板不存在: " + id);
        }
        return template;
    }

    /**
     * 查询模板的所有 Section
     */
    public List<PrmSectionEntity> getSections(String templateId) {
        return prmSectionMapper.selectByTemplateId(templateId);
    }

    /**
     * 读取 seed YAML
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readSeed() {
        Yaml yaml = new Yaml();
        try (InputStream in = new ClassPathResource("seed/prm-templates.yaml").getInputStream()) {
            Map<String, Object> data = yaml.load(in);
            return (List<Map<String, Object>>) data.get("templates");
        } catch (Exception e) {
            log.error("读取 seed/prm-templates.yaml 失败", e);
            return new ArrayList<>();
        }
    }
}
