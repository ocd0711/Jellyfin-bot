package com.ocd.mapper.mysql

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.ocd.bean.mysql.Line
import org.apache.ibatis.annotations.Mapper

/**
 * @author OCD
 * @date 2022/12/25 12:17
 * Description:
 */
@Mapper
interface LineMapper : BaseMapper<Line> {

}