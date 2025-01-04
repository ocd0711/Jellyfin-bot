package com.ocd.bean.dto.jellby

import com.alibaba.fastjson2.annotation.JSONCreator
import com.alibaba.fastjson2.annotation.JSONField
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ch.hu
 * @date 2025/01/04 16:25
 * Description:
 */
data class PlaybackData @JSONCreator constructor(
    @JSONField(name = "colums") val columns: List<String>,
    @JSONField(name = "results") val results: List<List<String>>,
    @JSONField(name = "message") val message: String
) {
    fun mapResultsToPlaybackRecords(): List<PlaybackRecord> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS", Locale.getDefault())

        return results.map { row ->
            val dateCreated = dateFormat.parse(row[0])

            PlaybackRecord(
                dateCreated = dateCreated,
                userId = row[1],
                itemId = row[2],
                itemType = row[3],
                itemName = row[4],
                playbackMethod = row[5],
                clientName = row[6],
                deviceName = row[7],
                playDuration = row[8]
            )
        }
    }
}