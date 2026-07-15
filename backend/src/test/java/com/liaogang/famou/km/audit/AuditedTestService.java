package com.liaogang.famou.km.audit;

/**
 * 集成测试用 @AuditLog 标注的服务样例。
 * <p>提供成功路径（doSomething）+ 失败路径（throwError）两个用例。
 * <p>仅用于集成测试 classpath（src/test/java），不进入 main。
 */
public class AuditedTestService {

    @AuditLog(action = "KO_UPDATE", remark = "更新 KO #p0 类型 #p1")
    public String doSomething(String koId, String type) {
        return "ok:" + koId + ":" + type;
    }

    @AuditLog(action = "KO_DELETE", remark = "删除 KO #p0")
    public void throwError(String koId) {
        throw new IllegalStateException("模拟业务异常: " + koId);
    }
}
