package com.ocd.controller.util

import com.ocd.bean.dto.result.PlaybackShowsResult
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.general.DefaultPieDataset
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.PieChart
import org.knowm.xchart.PieChartBuilder
import org.knowm.xchart.PieSeries
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * @author ch.hu
 * @date 2024/12/19 13:45
 * Description:
 */
object ChartUtil {

    /**
     * 根据 PlaybackShowsResult 数据生成饼图，并返回 InputFile（用于 Telegram Bot）
     *
     * @param data PlaybackShowsResult 数据列表
     * @param title 图表标题
     * @return InputFile 生成的饼图文件
     */
    @JvmStatic
    @JvmOverloads
    fun generatePieChartAsInputFile(
        data: List<PlaybackShowsResult>,
        title: String = "Playback Shows",
        height: Int = 720,
        width: Int = 1280
    ): InputFile {
        // 创建饼图
        val chart: PieChart = PieChartBuilder()
            .width(width)
            .height(height)
            .title(title)
            .build()

        // 配置饼图样式
        chart.styler.isLegendVisible = true
        chart.styler.isCircular = true
        chart.styler.defaultSeriesRenderStyle = PieSeries.PieSeriesRenderStyle.Pie
//        chart.styler.startAngleInDegrees = 90

        // 添加数据
        data.forEach { result ->
            chart.addSeries(result.label, result.count.toDouble())
        }

        // 转换为字节数组并包装成 InputFile
        ByteArrayOutputStream().use { baos ->
            BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG)
            val imageBytes = baos.toByteArray()
            return InputFile(ByteArrayInputStream(imageBytes), "chart.png")
        }
    }

    @JvmStatic
    @JvmOverloads
    fun generatePieChartAsInputFileNew(
        data: List<PlaybackShowsResult>,
        title: String = "Playback Shows",
        width: Int = 1280,
        height: Int = 720
    ): InputFile {
        // 创建饼图数据集
        val dataset = DefaultPieDataset()
        data.forEach { result ->
            dataset.setValue(result.label, result.count)
        }

        // 创建饼图
        val chart: JFreeChart = ChartFactory.createPieChart(
            title,          // 图表标题
            dataset,        // 数据集
            true,          // 显示图例
            true,          // 显示工具提示
            false           // 不生成 URL
        )

        // 将图表转换为 PNG 字节流
        ByteArrayOutputStream().use { baos ->
            writeChartAsPNG(baos, chart, width, height)
            val imageBytes = baos.toByteArray()
            // 包装为 InputFile
            return InputFile(ByteArrayInputStream(imageBytes), "chart.png")
        }
    }

    @JvmStatic
    private fun writeChartAsPNG(out: OutputStream, chart: JFreeChart, width: Int, height: Int) {
        // 创建 BufferedImage 用于保存图表图像
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        // 获取 Graphics2D 对象来绘制图表
        val graphics2D: Graphics2D = bufferedImage.createGraphics()

        // 启用抗锯齿以提高渲染质量
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // 绘制图表
        chart.draw(graphics2D, java.awt.Rectangle(0, 0, width, height))

        // 释放 Graphics2D 资源
        graphics2D.dispose()

        // 使用 ImageIO 将图像以 PNG 格式写入输出流
        ImageIO.write(bufferedImage, "PNG", out)
    }

    fun formatMinutesToTime(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = 0
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatSecondsToTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}