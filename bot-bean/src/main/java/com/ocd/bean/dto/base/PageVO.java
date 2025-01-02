package com.ocd.bean.dto.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;

/**
 * @author OCD
 * @date 2022/05/19 00:25
 * Description:
 * 分页查询统一返回结果
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = -182574099663892610L;


    /**
     * 当前页
     */
    private long currentPage;

    /**
     * 页大小
     */
    private long pageSize;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 总页数
     */
    private long totalPage;

    // 实体类 具体返回参数为准
//    private List<T> list;

    public void updatePageInfo(Page page) {
        this.currentPage = page.getCurrent();
        this.pageSize = page.getSize();
        this.totalCount = page.getTotal();
        this.totalPage = page.getPages();
    }

}