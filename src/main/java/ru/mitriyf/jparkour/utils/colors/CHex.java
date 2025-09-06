package ru.mitriyf.jparkour.utils.colors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CHex implements Colorizer {
    private final Pattern PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public String colorize(String message) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            String color = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : color.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace('&', '§');
    }
}