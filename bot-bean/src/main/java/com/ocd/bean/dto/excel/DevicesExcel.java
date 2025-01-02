package com.ocd.bean.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ttzero.excel.annotation.ExcelColumn;

/**
 * @author ch.hu
 * @date 2024/12/17 09:53
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevicesExcel {

    @ExcelColumn("TG ID")
    private String tgId;

    @ExcelColumn("EMBY NAME")
    private String embyName;

    @ExcelColumn("EMBY ID")
    private String embyId;

    @ExcelColumn("DEVICE COUNT")
    private Long deviceCount;

    @ExcelColumn("DEVICE INFO")
    private String deviceInfo;
}
