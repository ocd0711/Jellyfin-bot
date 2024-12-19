package com.ocd.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author OCD
 * @date 2022/03/29 14:48
 * Description:
 * Mybatis-plus 配置类(集成分页插件)
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //声明插件拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //声明分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置最大分页数， -1: 不受限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        // 溢出总页数后是否进行处理
        paginationInnerInterceptor.setOverflow(true);
        // 生成 countSql 优化掉 join 现在只支持 left join
        paginationInnerInterceptor.setOptimizeJoin(true);
        //添加到插件拦截器中
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
