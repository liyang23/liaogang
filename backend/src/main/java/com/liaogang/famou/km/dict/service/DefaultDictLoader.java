package com.liaogang.famou.km.dict.service;

import com.liaogang.famou.km.dict.repository.UnitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 字典启动门禁 (U9 / T310 / 部署门禁)
 *
 * <p>应用启动时 (CommandLineRunner 之前) 检查 9 预制量纲是否完整存在.
 * 缺失即抛异常退出 (启动失败) - 部署门禁 (plan §U9 Verification: "9 预制量纲缺失 → 启动失败").
 */
@Slf4j
@Component
@Order(0)  // 最早执行, 在其他 runner 之前
@RequiredArgsConstructor
public class DefaultDictLoader implements ApplicationRunner {

    /** 9 预制量纲 (plan §U9 行 760) */
    private static final int EXPECTED_UNIT_COUNT = 9;

    private final UnitMapper unitMapper;

    @Override
    public void run(ApplicationArguments args) {
        int actualCount = unitMapper.countAll();
        if (actualCount < EXPECTED_UNIT_COUNT) {
            String msg = String.format(
                    "Deploy gate failed: expected %d 预制量纲, found %d. " +
                    "请检查 V9009__create_dict_tables.sql 9 INSERT IGNORE seed 是否完整执行.",
                    EXPECTED_UNIT_COUNT, actualCount);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.info("DefaultDictLoader: {} 预制量纲 OK, deploy gate passed.", actualCount);
    }
}
