package com.ocd.util

import com.alibaba.fastjson.serializer.SerializerFeature
import com.alibaba.fastjson.support.config.FastJsonConfig
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.http.converter.xml.SourceHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset
import javax.annotation.PostConstruct
import javax.xml.transform.Source


/**
 * @author OCD
 * @date 2022/07/29 3:48 PM
 * Description:
 * restTemplate 发起请求通用工具类
 */
@Component
class HttpUtil {

    private object HttpUtilHoder {
        @JvmStatic
        var mInstance: HttpUtil = HttpUtil()
    }

    companion object {
        @JvmStatic
        fun getInstance(): HttpUtil {
            return HttpUtilHoder.mInstance
        }
    }

    @PostConstruct
    fun init() {
        HttpUtilHoder.mInstance = this
        HttpUtilHoder.mInstance.templateBuilder = this.templateBuilder
        // 字节数组 http消息转换器
        restTemplate.messageConverters.add(ByteArrayHttpMessageConverter())
        // string http消息转换器
        restTemplate.messageConverters.add(StringHttpMessageConverter())
        // 可以读写Resource的 http消息转换器 比如读取media、file之类的
        restTemplate.messageConverters.add(ResourceHttpMessageConverter())
        // Source http消息转换器 用于转换Source类型对象（DOMSource, SAXSource, StreamSource）
        restTemplate.messageConverters.add(SourceHttpMessageConverter<Source>())
        // 所有通用消息转换器
        restTemplate.messageConverters.add(AllEncompassingFormHttpMessageConverter())
        // 注入 fastjson 参数转换器
        val fastConverter = FastJsonHttpMessageConverter()
        val fastJsonConfig = FastJsonConfig()
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat)
        fastConverter.fastJsonConfig = fastJsonConfig
        fastConverter.defaultCharset = Charset.forName("UTF-8")
        val converter: HttpMessageConverter<*> = fastConverter
//        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
//        clientHttpRequestFactory.setConnectTimeout(10_000)
//        clientHttpRequestFactory.setReadTimeout(10_000)
//        restTemplate.requestFactory = clientHttpRequestFactory
        val requestFactory = HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build())
        requestFactory.setConnectTimeout(2_000)
        requestFactory.setReadTimeout(5_000)
        restTemplate.requestFactory = requestFactory
        restTemplate.messageConverters.add(converter)
    }

    private val logger = LoggerFactory.getLogger(HttpUtil::class.java)

    @Autowired
    private lateinit var templateBuilder: RestTemplateBuilder

    private val restTemplate: RestTemplate by lazy {
        templateBuilder.build()
    }

    fun restTemplate(): RestTemplate {
        return restTemplate
    }

}