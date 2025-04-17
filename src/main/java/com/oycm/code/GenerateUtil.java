package com.oycm.code;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @author ouyangcm
 * create 2024/12/16 13:12
 */
public class GenerateUtil {

    public static final String ANNOTATION_1 = "@Column(name = \"%s\")";


    public static String getFieldColumnName(String fieldName) {
        StringBuilder builder = new StringBuilder(fieldName.replace('.', '_'));
        for (int i = 1; i < builder.length() - 1; i++) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder.toString().toUpperCase();
    }

    private static boolean isUnderscoreRequired(char before, char current, char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
    }

    public static String getFieldName(String line) {
        String[] split = line.split(" ");
        if (split.length == 3) {
            return split[2].substring(0, split[2].length()-1);
        }
        return null;
    }

    public static BufferedReader getBufferedReader(String fileName) throws FileNotFoundException {
        FileReader fileReader = new FileReader(fileName);

        BufferedReader reader = new BufferedReader(fileReader);

        return reader;
    }





}
