package com.ocd.service.mysql

import com.ocd.mapper.mysql.InfoMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author OCD
 * @date 2022/12/24 1:04
 * Description:
 */
@Service
class InfoService {

    @Autowired
    lateinit var infoMapper: InfoMapper

}