package com.ocd.service.mysql

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.ocd.bean.mysql.Devices
import com.ocd.mapper.mysql.DevicesMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author ch.hu
 * @date 2024/12/17 09:35
 * Description:
 */
@Service
class DevicesService {

    @Autowired
    lateinit var devicesMapper: DevicesMapper

    fun getDeviceList(uuid: String?): List<Devices> {
//        c38d581603384002bde8976f6aeee5bd
//        c38d5816-0338-4002-bde8-976f6aeee5bd
        uuid ?: return emptyList()
        return devicesMapper.selectList(
            QueryWrapper<Devices>().lambda().eq(
                Devices::getUserId,
                uuid.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)".toRegex(),
                    "$1-$2-$3-$4-$5"
                )
            )
        )
    }

    fun getDeviceCount(uuid: String?): Long {
//        c38d581603384002bde8976f6aeee5bd
//        c38d5816-0338-4002-bde8-976f6aeee5bd
        uuid ?: return 0
        return devicesMapper.selectCount(
            QueryWrapper<Devices>().lambda().eq(
                Devices::getUserId,
                uuid.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)".toRegex(),
                    "$1-$2-$3-$4-$5"
                )
            )
        )
    }
}