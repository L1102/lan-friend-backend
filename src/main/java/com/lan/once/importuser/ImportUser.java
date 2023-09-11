package com.lan.once.importuser;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lan
 */
public class ImportUser {


    public static void main(String[] args) {
        String fileName = "";
        List<UserTableInfo> userInfoList = EasyExcel.read(fileName).head(UserTableInfo.class).sheet().doReadSync();
        System.out.println("总数= " + userInfoList.size());
        Map<String, List<UserTableInfo>> listMap = userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(UserTableInfo::getUsername));
        for (Map.Entry<String, List<UserTableInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                System.out.println("username = " + stringListEntry.getKey());
                System.out.println("1");
            }
        }
        System.out.println("不重复昵称数 = " + listMap.keySet().size());

    }
}
