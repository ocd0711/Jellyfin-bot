package com.ocd.controller.util;

import com.isen.bean.constant.ConstantStrings;
import kotlin.jvm.JvmOverloads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    public RedisUtil(RedisTemplate redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    private static RedisTemplate redisTemplate;

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @param time  自删时间, 为 null 不会自删(单位: 秒)
     * @return true成功 false失败
     */
    public static boolean set(String key, Object value, Long time) {
        try {
            if (time != null)
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            else
                redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存存在判断
     *
     * @param key
     * @return
     */
    public static Boolean contain(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 普通缓存移除
     *
     * @param key
     * @return
     */
    public static Object del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 写入 redis 缓存(hash)
     *
     * @param key
     * @param value
     * @param redisType 精准定位:ConstantStrings.PRECISE_POSITIONING_LIST 设备配置:ConstantStrings.DEVICE_SETTING_COMMAND_CACHE
     * @return
     */
    public static boolean setRedisHashCache(String key, Object value, String redisType) {
        try {
            redisTemplate.opsForHash().put(ConstantStrings.INSTANCE.getRedisTypeKey("", redisType), key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除 redis 缓存(hash)
     *
     * @param key
     * @param redisType 精准定位:ConstantStrings.PRECISE_POSITIONING_LIST 设备配置:ConstantStrings.DEVICE_SETTING_COMMAND_CACHE
     */
    public static boolean removeRedisHashCache(String key, String redisType) {
        try {
            redisTemplate.opsForHash().delete(ConstantStrings.INSTANCE.getRedisTypeKey("", redisType), key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
