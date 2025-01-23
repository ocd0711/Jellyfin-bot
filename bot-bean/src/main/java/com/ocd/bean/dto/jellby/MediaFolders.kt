package com.ocd.bean.dto.jellby

/**
 * @author ch.hu
 * @date 2025/01/23 16:44
 * Description:
 */
class MediaFolders(
    val Guid: String,
    val Id: String,
    val IsUserAccessConfigurable: Boolean,
    val Name: String,
    val SubFolders: List<SubFolder>
)

data class SubFolder(
    val Id: String,
    val IsUserAccessConfigurable: Boolean,
    val Name: String,
    val Path: String
)