package com.ocd.controller.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.ocd.util.FormatUtil;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * @author OCD
 * @date 2022/04/08 23:19
 * Description:
 * 添加fastjson的转换
 * 完全接管的springmvc, 默认配置全部失效, 改用bean替代默认转换器 By OCD
 * <p>
 * 消息转换自动化配置类 HttpMessageConvertersAutoConfiguration
 * @Configuration( proxyBeanMethods = false
 * )
 * @ConditionalOnClass({HttpMessageConverter.class})
 * @Conditional({HttpMessageConvertersAutoConfiguration.NotReactiveWebApplicationCondition.class})
 * @AutoConfigureAfter({GsonAutoConfiguration.class, JacksonAutoConfiguration.class, JsonbAutoConfiguration.class})
 * @Import({JacksonHttpMessageConvertersConfiguration.class, GsonHttpMessageConvertersConfiguration.class, JsonbHttpMessageConvertersConfiguration.class})
 * public class HttpMessageConvertersAutoConfiguration {
 * static final String PREFERRED_MAPPER_PROPERTY = "spring.http.converters.preferred-json-mapper";
 * <p>
 * public HttpMessageConvertersAutoConfiguration() {
 * }
 * @Bean
 * @ConditionalOnMissingBean public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
 * return new HttpMessageConverters((Collection)converters.orderedStream().collect(Collectors.toList()));
 * }
 * <p>
 * 在声明 HttpMessageConverters 的方法上发现 @ConditionalOnMissingBean 注解，也就是当程序中没有声明 HttpMessageConverters 的 bean 时，才会加载默认的这些 converters。
 * 如果程序中声明了自己的 HttpMessageConverters，那么就不会加载默认的这些 converters。
 */
@Configuration
public class FastjsonConverter {

    @Bean
    public HttpMessageConverters customConverters() {
        // 定义一个转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        // 添加fastjson的配置信息 比如 ：是否要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        // 这里就是核心代码了，WriteMapNullValue把空的值的key也返回
        fastJsonConfig.setSerializerFeatures(
                // 保留map空的字段
                SerializerFeature.WriteMapNullValue,
                // 将String类型的null转成""
                SerializerFeature.WriteNullStringAsEmpty,
                // 将Number类型的null转成0
                SerializerFeature.WriteNullNumberAsZero,
                // 将List类型的null转成[]
                SerializerFeature.WriteNullListAsEmpty,
                // 将Boolean类型的null转成false
                SerializerFeature.WriteNullBooleanAsFalse,
                // 默认的Date转换
                SerializerFeature.WriteDateUseDateFormat,
                // 避免循环引用
                SerializerFeature.DisableCircularReferenceDetect);

        JSON.defaultTimeZone = TimeZone.getTimeZone("GMT+8");
        //如果时间类型值为null，则返回空串
        ValueFilter dateFilter = (Object var1, String var2, Object var3) -> {
            try {
                if (var3 == null && var1 != null && Date.class.isAssignableFrom(var1.getClass().getDeclaredField(var2).getType())) {
                    return "";
                }
            } catch (Exception e) {
            }
            return var3;
        };
        fastJsonConfig.setSerializeFilters(dateFilter);

        fastJsonConfig.setDateFormat(FormatUtil.INSTANCE.getDateS());

        List<MediaType> fastMediaTypes = new ArrayList<MediaType>();

        // 处理中文乱码问题
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        // 在转换器中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);

        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setDefaultCharset(Charset.forName("UTF-8"));
        stringConverter.setSupportedMediaTypes(fastMediaTypes);

        // 将转换器添加到converters中
        return new HttpMessageConverters(stringConverter, fastConverter);
    }
}