package com.ocd.bean.dto.result;

import com.ocd.bean.dto.moviepilot.MoviepilotResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2025/02/17 17:37
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheBotSearchFilm {

    private Integer currentPage = 0;

    private Integer pageSize = 5;

    private List<MoviepilotResult> moviepilotResults;

    public List<MoviepilotResult> getPagedResults() {
        if (moviepilotResults == null || moviepilotResults.isEmpty()) {
            return List.of();
        }

        int fromIndex = getCurrentPage() * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, moviepilotResults.size());

        if (fromIndex >= moviepilotResults.size()) {
            return List.of();
        }

        currentPage += 1;
        return moviepilotResults.subList(fromIndex, toIndex);
    }

    public String getPageInfo() {
        if (moviepilotResults == null || moviepilotResults.isEmpty()) {
            return "第 0 页 / 共 0 页, 0 个资源";
        }

        int totalRecords = moviepilotResults.size();
        int totalPages = (totalRecords + pageSize - 1) / pageSize;
        int currentPage = getCurrentPage();

        return String.format("请点击下载按钮选择下载，如果没有合适的资源，请翻页查询\n" +
                "\n" +
                "第 %d 页 / 共 %d 页, %d 个资源", currentPage, totalPages, moviepilotResults.size());
    }
}