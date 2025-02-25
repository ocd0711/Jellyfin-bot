package com.ocd.service.mysql

import com.ocd.bean.mysql.Moviepilot
import com.ocd.mapper.mysql.MoviepilotMapper
import org.springframework.stereotype.Service

/**
 * @author ch.hu
 * @date 2025/02/17 16:49
 * Description:
 */
@Service
class MoviepilotService(
    val moviepilotMapper: MoviepilotMapper
) {

    fun createMoviepilot(moviepilot: Moviepilot): Long {
        moviepilotMapper.insert(moviepilot)
        return moviepilot.id
    }
}