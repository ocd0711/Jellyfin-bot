package com.ocd.mapper.mysql

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.ocd.bean.mysql.Info
import org.apache.ibatis.annotations.Mapper

/**
 * @author OCD
 * @date 2022/12/24 1:04
 * Description:
 */
@Mapper
interface InfoMapper : BaseMapper<Info> {

}