package com.ocd.util

import java.text.SimpleDateFormat
import java.util.*


/**
 * @author OCD
 * @date 2022/03/30 13:05
 * Description:
 * 数据格式化工具类
 */
object FormatUtil {

    val dateS = "yyyy/MM/dd HH:mm:ss"

    val dateShow = "yyyy/MM/dd"

    val sdf = SimpleDateFormat(dateS)

    val sdfShow = SimpleDateFormat(dateShow)

    val other = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")

    val quectelDate = SimpleDateFormat("yyyy-MM-dd")

    val noGpsString = SimpleDateFormat("yyyyMMddHHmmss")

    val mqttTimeString = SimpleDateFormat("yyyyMMddHHmmssSSS")

    fun formatLongTimeToDate(time: Long): Date {
        return Date(time)
    }

    fun formatStringTimeToDate(time: String): Date {
        return sdf.parse(time.replace("/", "-")) as Date
    }

    fun formatOtherStringTimeToDate(time: String?): String {
        time ?: return "暂无记录"
        return (other.parse(time) as Date).toString()
    }

    fun formatNoGpsStringTimeToDate(time: String): Date {
        return noGpsString.parse(time) as Date
    }

    fun formatMqttTimeStringTimeToDate(time: String): Date {
        return mqttTimeString.parse(time) as Date
    }

    fun getNowTimeString(): String {
        return sdf.format(Date())
    }

    fun getQuectelDate(time: String): Date {
        return quectelDate.parse(time)
    }

    fun dateToString(date: Date?): String {
        date ?: return "暂无记录"
        return sdf.format(date)
    }

    @JvmOverloads
    fun dateToShowString(date: Date? = null): String {
        return sdfShow.format(date ?: Date())
    }

    fun formatOtherStringTimeToDateStr(time: String?): String {
        time ?: return "暂无记录"
        return dateToString(other.parse(time) as Date)
    }

    fun nowTimeMqtt(): String {
        return mqttTimeString.format(Date())
    }

    fun replaceSpecialCharacters(content: String, replacement: String): String {
        return content.replace("[\\pP\\p{Punct}]".toRegex(), replacement)
    }

}