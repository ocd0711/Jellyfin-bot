package com.ocd.service.mysql

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.ocd.bean.mysql.ActivityLogs
import com.ocd.mapper.mysql.ActivityLogsMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * @author ch.hu
 * @date 2024/11/16 21:45
 * Description:
 */
@Service
class ActivityLogsService {

    @Autowired
    lateinit var activityLogsMapper: ActivityLogsMapper

    fun getLastPlay(uuid: String): ActivityLogs? {
//        c38d581603384002bde8976f6aeee5bd
//        c38d5816-0338-4002-bde8-976f6aeee5bd
        return activityLogsMapper.selectOne(
            QueryWrapper<ActivityLogs>().lambda().eq(
                ActivityLogs::getUserId,
                uuid.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)".toRegex(),
                    "$1-$2-$3-$4-$5"
                )
            ).eq(ActivityLogs::getType, "VideoPlayback")
                .orderByDesc(ActivityLogs::getDateCreated)
        )
    }
}