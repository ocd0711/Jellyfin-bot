package com.ocd.service.mysql

import com.ocd.mapper.mysql.LineMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author OCD
 * @date 2022/12/25 12:18
 * Description:
 */
@Service
class LineService {

    @Autowired
    lateinit var lineMapper: LineMapper

}