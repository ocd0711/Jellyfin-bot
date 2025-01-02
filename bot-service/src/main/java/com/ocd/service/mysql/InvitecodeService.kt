package com.ocd.service.mysql

import com.ocd.mapper.mysql.InvitecodeMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author OCD
 * @date 2023/02/14 4:05 PM
 * Description:
 */
@Service
class InvitecodeService {

    @Autowired
    lateinit var invitecodeMapper: InvitecodeMapper

}