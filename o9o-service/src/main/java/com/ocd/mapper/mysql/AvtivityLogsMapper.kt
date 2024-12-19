package com.ocd.mapper.mysql

import com.baomidou.dynamic.datasource.annotation.DS
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.ocd.bean.mysql.ActivityLogs
import org.apache.ibatis.annotations.Mapper

/**
 * @author ch.hu
 * @date 2024/11/16 21:44
 * Description:
 */
@Mapper
@DS("jellyfin")
interface ActivityLogsMapper : BaseMapper<ActivityLogs> {
}