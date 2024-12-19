package com.ocd.service.mysql

import com.ocd.mapper.mysql.HideMediaMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author OCD
 * @date 2023/02/16 1:31 PM
 * Description:
 */
@Service
class HideMediaService {

    @Autowired
    lateinit var hideMediaMapper: HideMediaMapper

}