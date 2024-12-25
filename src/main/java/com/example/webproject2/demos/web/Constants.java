package com.example.webproject2.demos.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    // 不可变的全局 Map
    public static final Map<String, String> TYPE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("farmhouse", "农家院");
        map.put("nature", "自然风光");
        map.put("ancient", "古建筑");
        map.put("specialty", "土特产");
        map.put("food", "特色小吃");
        map.put("folk", "民俗活动");

        // 创建不可变的 Map
        TYPE_MAP = Collections.unmodifiableMap(map);
    }
}