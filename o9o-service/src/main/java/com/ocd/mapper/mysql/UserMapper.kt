package com.ocd.mapper.mysql

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.ocd.bean.mysql.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * @author OCD
 * @date 2022/03/29 14:28
 * Description:
 * 用户表
 */
@Mapper
interface UserMapper : BaseMapper<User> {

    fun selectByCondition(page: IPage<User>, @Param("fuzzyQuery") fuzzyQuery: String?): List<User>?

}