package com.dirkarnez.singlepageappserver;

import java.util.regex.Pattern;

public class Constants {
    public static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";

    public static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";

    public static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";

    public static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX);

    public static final String CONTENT_LENGTH_REGEX = "Content-Length:";
    public static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile(CONTENT_LENGTH_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String USER_AGENT = "User-Agent:";
    public static final Pattern USER_AGENT_PATTERN = Pattern.compile(USER_AGENT, Pattern.CASE_INSENSITIVE);

    public static final String HOST_REGEX = "Host:";
    public static final Pattern CLIENT_HOST_PATTERN = Pattern.compile(HOST_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONNECTION_TYPE_REGEX = "Connection:";
    public static final Pattern CONNECTION_TYPE_PATTERN = Pattern.compile(CONNECTION_TYPE_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String ACCEPT_ENCODING_REGEX = "Accept-Encoding:";
    public static final Pattern ACCEPT_ENCODING_PATTERN = Pattern.compile(ACCEPT_ENCODING_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";

    private static final Pattern MIME_PATTERN = Pattern.compile(CONTENT_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";

    private static final Pattern CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";

    private static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE);

}
