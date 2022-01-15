package com.github.Dewynion.embercore.util;

import com.github.Dewynion.embercore.EmberCore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {
    public static String fString(String s, Object obj) {
        Map<String, Object> data = new HashMap<>();
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                data.put(field.getName(), field.get(obj));
            }
        } catch (IllegalAccessException ex) {
            EmberCore.warn("Unable to access fields for type %s while reflectively populating fstring.", obj.getClass().getName());
        }
        return fString(s, data);
    }

    public static String fString(String s, Map<String, Object> data){
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(s);

        while(matcher.find()){
            Object value = data.get(matcher.group(1));
            if(value != null) {
                s = s.replace(matcher.group(0), value.toString());
            }
        }

        return s;
    }
}
