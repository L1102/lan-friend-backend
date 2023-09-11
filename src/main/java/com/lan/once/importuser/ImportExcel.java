package com.lan.once.importuser;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author lan
 */
public class ImportExcel {

    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "";
        synchronousRead(fileName);
    }

    /**
     * 监听器读取
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, UserTableInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * @param fileName
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserTableInfo> totalDataList = EasyExcel.read(fileName)
                .head(UserTableInfo.class).sheet().doReadSync();
        for (UserTableInfo userTableInfo : totalDataList) {
            System.out.println(userTableInfo);
        }
    }
}
