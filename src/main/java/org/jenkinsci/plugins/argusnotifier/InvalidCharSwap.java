package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableList;

import java.util.regex.Pattern;

/**
 * This class will replace invalid characters with valid characters
 */
public class InvalidCharSwap {
    private static final ImmutableList<String> INVALID_STRINGS = ImmutableList.of("%2F");
    private static final String INVALID_CHARS = Pattern.compile("[^a-zA-Z_0-9.\\-/]").toString();

    /**
     * Swaps invalid characters with a dash
     *
     * @param input input string
     * @return output string with invalid characters swapped out with a dash
     */
    public static String swapWithDash(String input) {
        String outputString = input;
        for (String invalidString : INVALID_STRINGS) {
            outputString = outputString.replace(invalidString, "-");
        }
        outputString = outputString.replaceAll(INVALID_CHARS, "-");
        return outputString;
    }
}
