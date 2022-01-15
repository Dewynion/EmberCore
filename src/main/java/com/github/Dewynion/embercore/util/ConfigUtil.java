package com.github.Dewynion.embercore.util;

public final class ConfigUtil {
    public static String camelCaseToSnakeCase(String camelCaseName) {
        // convert the field name into yml case by splitting CamelCase at uppercase letters
        String[] pathRegex = camelCaseName.split("(?=\\p{Upper})");
        StringBuilder ymlPath = new StringBuilder();
        // then stitch components together with "-"
        for (String s : pathRegex)
            ymlPath.append(s.toLowerCase()).append("-");
        // there'll be an unnecessary "-" at the end so cut it off
        return ymlPath.substring(0, ymlPath.length() - 1);
    }
}
