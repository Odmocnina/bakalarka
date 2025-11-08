package core.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public class StringEditor {

    public static String mergeRequestParameters(String params1, String params2) {
        if (params1 == null || params1.isEmpty()) {
            return params2;
        }
        if (params2 == null || params2.isEmpty()) {
            return params1;
        }
        if (params1.equals(params2)) {
            return params1;
        }

        String[] requestParams1 = params1.split(Constants.REQUEST_SEPARATOR);
        String[] requestParams2 = params2.split(Constants.REQUEST_SEPARATOR);

        Set<String> mergedSet = new LinkedHashSet<>(); // to maintain order and uniqueness, it throws away duplicates

        for (String p : requestParams1) {
            p = p.trim();
            if (!p.isEmpty()) {
                mergedSet.add(p);
            }
        }

        for (String p : requestParams2) {
            p = p.trim();
            if (!p.isEmpty()) {
                mergedSet.add(p);
            }
        }

        return String.join(Constants.REQUEST_SEPARATOR, mergedSet);

    }

    public static boolean isInArray(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
