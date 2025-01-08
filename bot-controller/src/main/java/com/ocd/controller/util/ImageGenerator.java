package com.ocd.controller.util;

import com.ocd.bean.dto.jellby.PlaybackRecord;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * @author ch.hu
 * @date 2025/01/07 16:55
 * Description:
 */
public class ImageGenerator {

    private static final int ITEM_PADDING = 20; // 每个项目的间距
    private static final int TEXT_PADDING = 10; // 文字与图片的间距
    private static final Color TEXT_COLOR = Color.WHITE; // 文字颜色

    public static InputFile generateRankingImage(
            boolean isWeekly, List<PlaybackRecord> movieShows, List<PlaybackRecord> tvShows, int width) throws IOException {

        // 动态计算最小高度
        int minHeight = calculateMinHeight(width, movieShows, tvShows);

        BufferedImage image = new BufferedImage(width, minHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 启用抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制随机渐变背景
        drawRandomGradientBackground(g, width, minHeight);

        // 标题高度
        int titleHeight = 70;

        // 绘制标题
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = isWeekly ? "WEEKLY TOP" : "DAILY TOP";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.setColor(TEXT_COLOR);
        g.drawString(title, (width - titleWidth) / 2, titleHeight - 20);

        // 动态绘制 Movies 分类
        int moviesStartY = titleHeight + ITEM_PADDING;
        int moviesUsedHeight = drawCategory(true, g, "MOVIES", movieShows, width, moviesStartY);

        // 动态绘制 TV Shows 分类
        int tvShowsStartY = moviesStartY + moviesUsedHeight + ITEM_PADDING;
        drawCategory(false, g, "TV SHOWS", tvShows, width, tvShowsStartY);

        // 保存图片
        g.dispose();
//        File outputFile = new File("/Users/ch.hu/Downloads/ranking.png");
//        ImageIO.write(image, "png", outputFile);

        // 保存图像到 ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();

        // 返回 InputFile
        return new InputFile(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "dynamic_poster.png");
    }

    private static void drawRandomGradientBackground(Graphics2D g, int width, int height) {
        Random random = new Random();
        Color color1 = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Color color2 = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);
    }

    private static int drawCategory(boolean isMovie, Graphics2D g, String category, List<PlaybackRecord> shows, int canvasWidth, int startY) {
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(TEXT_COLOR);
        int categoryWidth = g.getFontMetrics().stringWidth(category);
        g.drawString(category, (canvasWidth - categoryWidth) / 2, startY);

        // 最大行项目数
        int maxItemsPerRow = 5;
        int availableWidth = canvasWidth - (ITEM_PADDING * 2);
        int itemWidth = (availableWidth - ((maxItemsPerRow - 1) * ITEM_PADDING)) / maxItemsPerRow;
        int itemHeight = itemWidth * 4 / 3; // 按比例计算图片高度
        int rowHeight = itemHeight + 50 + ITEM_PADDING; // 每行高度

        // 绘制网格
        int x = ITEM_PADDING;
        int y = startY + 50; // 分类标题后的初始 Y 坐标
        for (int i = 0; i < shows.size(); i++) {
            if (i > 0 && i % maxItemsPerRow == 0) {
                x = ITEM_PADDING; // 换行重置 X 坐标
                y += rowHeight;  // 换行增加 Y 坐标
            }

            PlaybackRecord show = shows.get(i);

            // 绘制封面图片
            BufferedImage cover = EmbyUtil.getInstance().getItemBackdrop(show.getItemId(), itemWidth);
            if (cover != null) {
                g.drawImage(cover, x, y, itemWidth, itemHeight, null);
            }

            // 绘制标题文字
            String showName = isMovie ? show.getItemName() : show.getName();
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(TEXT_COLOR);

            FontMetrics metrics = g.getFontMetrics();
            int nameWidth = metrics.stringWidth(showName);
            int availableWidthForText = itemWidth - (2 * TEXT_PADDING);

            if (nameWidth > availableWidthForText) {
                showName = truncateText(showName, availableWidthForText, g);
            }
            g.drawString(showName, x + TEXT_PADDING, y + itemHeight + 20);

            x += itemWidth + ITEM_PADDING; // 下一个项目的 X 坐标
        }

        return y + rowHeight - startY; // 返回分类总高度
    }

    private static String truncateText(String text, int maxWidth, Graphics2D g) {
        String ellipsis = "...";
        FontMetrics metrics = g.getFontMetrics();
        int ellipsisWidth = metrics.stringWidth(ellipsis);
        int textWidth = metrics.stringWidth(text);

        if (textWidth <= maxWidth) {
            return text;
        }

        while (textWidth + ellipsisWidth > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            textWidth = metrics.stringWidth(text);
        }
        return text + ellipsis;
    }

    private static int calculateMinHeight(int width, List<PlaybackRecord> movieShows, List<PlaybackRecord> tvShows) {
        int titleHeight = 70; // 标题高度
        int categoryPadding = ITEM_PADDING; // 分类之间的额外间距

        // 最大行项目数
        int maxItemsPerRow = 5;
        int availableWidth = width - (ITEM_PADDING * 2); // 减去左右边距
        int itemWidth = (availableWidth - ((maxItemsPerRow - 1) * ITEM_PADDING)) / maxItemsPerRow;
        int itemHeight = itemWidth * 4 / 3; // 图片高度按比例计算

        // 每行总高度 = 图片高度 + 文字高度 + 间距
        int rowHeight = itemHeight + 50 + ITEM_PADDING;

        // Movies 和 TV Shows 的总高度
        int moviesHeight = calculateCategoryHeight(movieShows, itemWidth, maxItemsPerRow, rowHeight);
        int tvShowsHeight = calculateCategoryHeight(tvShows, itemWidth, maxItemsPerRow, rowHeight);

        // 总高度 = 标题高度 + 分类高度 + 分类之间的间距
        return titleHeight + moviesHeight + tvShowsHeight + (categoryPadding * 3);
    }

    private static int calculateCategoryHeight(List<PlaybackRecord> shows, int itemWidth, int maxItemsPerRow, int rowHeight) {
        int rows = (int) Math.ceil((double) shows.size() / maxItemsPerRow); // 总行数
        return rows * rowHeight + 50; // 包含分类标题的额外高度
    }
}