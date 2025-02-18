package com.ocd.service.mysql

import com.baomidou.mybatisplus.core.metadata.IPage
import com.ocd.bean.mysql.User
import com.ocd.mapper.mysql.UserMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


/**
 * @author OCD
 * @date 2022/03/30 17:20
 * Description:
 */
@Service
class UserService {

    @Autowired
    lateinit var userMapper: UserMapper

    fun createUser(user: User): Long {
        userMapper.insert(user)
        return user.id
    }

    fun selectByCondition(page: IPage<User>, fuzzyQuery: String?): List<User>? {
        return userMapper.selectByCondition(page, fuzzyQuery)
    }

}