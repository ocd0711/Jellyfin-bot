package com.ocd.service.mysql

import com.ocd.mapper.mysql.ShopMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author OCD
 * @date 2023/02/14 2:12 PM
 * Description:
 */
@Service
class ShopService {

    @Autowired
    lateinit var shopMapper: ShopMapper

}