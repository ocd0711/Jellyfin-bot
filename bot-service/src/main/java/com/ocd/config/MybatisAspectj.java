package com.ocd.config;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author OCD
 * @date 2022/04/08 14:30
 * Description:
 * 使用 AOP 实现 mybatis-plus selectOne 方法的切面 默认仅查询一条数据
 * 是否保留视情况而定!!!!!
 */
@Aspect
@Component
public class MybatisAspectj {

    @Pointcut("execution(public * com.baomidou.mybatisplus.core.mapper.BaseMapper.selectOne(..))")
    public void selectOneAspect() {
    }

    @Before("selectOneAspect()")
    public void beforeSelect(JoinPoint point) {
        Object arg = point.getArgs()[0];
        if (arg instanceof AbstractWrapper) {
            arg = (AbstractWrapper) arg;
            ((AbstractWrapper) arg).last("limit 1");
        }
    }

}