package com.ocd.mapper.mysql

import com.baomidou.dynamic.datasource.annotation.DS
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.ocd.bean.mysql.Devices
import org.apache.ibatis.annotations.Mapper

/**
 * @author ch.hu
 * @date 2024/12/17 09:34
 * Description:
 */
@Mapper
@DS("jellyfin")
interface DevicesMapper : BaseMapper<Devices> {
}