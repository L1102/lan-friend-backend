package com.lan.once.importuser;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author lan
 */
@Data
public class UserTableInfo {
    //
    // /**
    //  * id
    //  */
    // @ExcelProperty("成员编号")
    // private String planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;

}

