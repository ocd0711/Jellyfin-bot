package com.ocd.mapper.mysql

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.ocd.bean.mysql.Shop
import org.apache.ibatis.annotations.Mapper

/**
 * @author OCD
 * @date 2023/02/14 2:12 PM
 * Description:
 */
@Mapper
interface ShopMapper : BaseMapper<Shop> {

}