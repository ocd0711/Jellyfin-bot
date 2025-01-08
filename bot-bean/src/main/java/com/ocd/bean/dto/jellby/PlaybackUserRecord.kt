package com.ocd.bean.dto.jellby

import com.alibaba.fastjson2.annotation.JSONField
import java.util.*

/**
 * @author ch.hu
 * @date 2025/01/04 15:48
 * Description:
 */
data class PlaybackUserRecord(
    @JSONField(name = "DateCreated") val dateCreated: Date,
    @JSONField(name = "UserId") val userId: String,
    @JSONField(name = "ItemId") val itemId: String,
    @JSONField(name = "ItemType") val itemType: String,
    @JSONField(name = "ItemName") val itemName: String,
    @JSONField(name = "PlaybackMethod") val playbackMethod: String,
    @JSONField(name = "ClientName") val clientName: String,
    @JSONField(name = "DeviceName") val deviceName: String,
    @JSONField(name = "PlayDuration") val playDuration: String
)