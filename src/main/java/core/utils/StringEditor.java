package core.utils;

import java.util.LinkedHashSet;
import java.util.Set;

/************************
 * Utility class for editing and manipulating strings, particularly for merging request parameters.
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
public class StringEditor {

    /**
     * Merges two strings of request parameters (one for forward, second for lane change), removing duplicates and
     * maintaining order.
     *
     * @param params1 The first string of request parameters.
     * @param params2 The second string of request parameters.
     * @return A merged string of request parameters without duplicates.
     **/
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

        String[] requestParams1 = params1.split(RequestConstants.REQUEST_SEPARATOR);
        String[] requestParams2 = params2.split(RequestConstants.REQUEST_SEPARATOR);

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

        return String.join(RequestConstants.REQUEST_SEPARATOR, mergedSet);

    }

    /**
     * Checks if a given value is present in the provided array.
     *
     * @param array The array to search.
     * @param value The value to find.
     * @return true if the value is found in the array, false otherwise.
     **/
    public static boolean isInArray(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
