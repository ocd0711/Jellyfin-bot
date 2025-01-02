package com.ocd.bean.dto.base;

import lombok.Data;

/**
 * @author OCD
 * @date 2022/12/09 16:53
 * Description:
 */
@Data
public class PageInfo {

    /**
     * 当前页号
     */
    private Integer currentPage = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

}
