package com.dirkarnez.singlepageappserver;

import android.util.Log;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

import static com.dirkarnez.singlepageappserver.Constants.ACCEPT_ENCODING_PATTERN;
import static com.dirkarnez.singlepageappserver.Constants.CLIENT_HOST_PATTERN;
import static com.dirkarnez.singlepageappserver.Constants.CONNECTION_TYPE_PATTERN;
import static com.dirkarnez.singlepageappserver.Constants.CONTENT_LENGTH_PATTERN;
import static com.dirkarnez.singlepageappserver.Constants.CONTENT_TYPE_PATTERN;
import static com.dirkarnez.singlepageappserver.Constants.USER_AGENT_PATTERN;

public class TinyWebServer extends Thread {
    public static final String TAG = TinyWebServer.class.getName();

    private static ServerSocket serverSocket;

    public static String CONTENT_TYPE = "text/html";
    private String CONTENT_DATE = "";
    private String CONN_TYPE = "";
    private String Content_Encoding = "";
    private String content_length = "";
    private String STATUS = "200";
    private boolean keepAlive = true;
    private String SERVER_NAME = "Firefly http server v0.1";
    private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
    private static final String ASCII_ENCODING = "US-ASCII";
    public String REQUEST_TYPE = "GET";
    private String HTTP_VER = "HTTP/1.1";

    //all status
    public static String PAGE_NOT_FOUND = "404";
    public static String OKAY = "200";
    public static String CREATED = "201";
    public static String ACCEPTED = "202";
    public static String NO_CONTENT = "204";
    public static String PARTIAL_NO_CONTENT = "206";
    public static String MULTI_STATUS = "207";
    public static String MOVED_PERMANENTLY = "301";
    public static String SEE_OTHER = "303";
    public static String NOT_MODIFIED = "304";
    public static String TEMP_REDIRECT = "307";
    public static String BAD_REQUEST = "400";
    public static String UNAUTHORIZED_REQUEST = "401";
    public static String FORBIDDEN = "403";
    public static String NOT_FOUND = "404";
    public static String METHOD_NOT_ALLOWED = "405";
    public static String NOT_ACCEPTABLE = "406";
    public static String REQUEST_TIMEOUT = "408";
    public static String CONFLICT = "409";
    public static String GONE = "410";
    public static String LENGTH_REQUIRED = "411";
    public static String PRECONDITION_FAILED = "412";

    public static String PAYLOAD_TOO_LARGE = "413";
    public static String UNSUPPORTED_MEDIA_TYPE = "415";
    public static String RANGE_NOT_SATISFIABLE = "416";
    public static String EXPECTATION_FAILED = "417";
    public static String TOO_MANY_REQUESTS = "429";

    public static String INTERNAL_ERROR = "500";
    public static String NOT_IMPLEMENTED = "501";
    public static String SERVICE_UNAVAILABLE = "503";
    public static String UNSUPPORTED_HTTP_VERSION = "505";

    public static String WEB_DIR_PATH = "/";
    public static int SERVER_PORT = 9000;
    public static boolean isStart = true;
    public static String INDEX_FILE_NAME = "index.html";


    public TinyWebServer(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);  //set timeout for listener
    }

    @Override
    public void run() {
        while (isStart) {
            try {
                Socket newSocket = serverSocket.accept();
                new EchoThread(newSocket).start();
            } catch (SocketTimeoutException s) {
            } catch (IOException e) {
            }
        } // Never Ending while loop
    }

    public class EchoThread extends Thread {
        protected Socket socket;

        public EchoThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = null;
                DataOutputStream out = null;

                if (socket.isConnected()) {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                }

                byte[] data = new byte[1500];
                //socket.setSoTimeout(60 * 1000 * 5);

                while (in.read(data) != -1 && isStart) {
                    String recData = new String(data).trim();
                    //System.out.println("received data: \n" + recData);
                    //System.out.println("------------------------------");
                    String[] header = recData.split("\\r?\\n");

                    String contentLength = "0";
                    String contentType = "text/html";
                    String connectionType = "keep-alive";
                    String hostname = "";
                    String userAgent = "";
                    String encoding = "";

                    String[] h1 = header[0].split(" ");
                    if (h1.length == 3) {
                        setRequestType(h1[0]);
                        setHttpVer(h1[2]);
                    }

                    for (int h = 0; h < header.length; h++) {
                        String value = header[h].trim();

                        //System.out.println(header[h]+" -> "+CONTENT_LENGTH_PATTERN.matcher(header[h]).find());
                        if (CONTENT_LENGTH_PATTERN.matcher(value).find()) {
                            contentLength = value.split(":")[1].trim();
                        } else if (CONTENT_TYPE_PATTERN.matcher(value).find()) {
                            contentType = value.split(":")[1].trim();
                        } else if (CONNECTION_TYPE_PATTERN.matcher(value).find()) {
                            connectionType = value.split(":")[1].trim();
                        } else if (CLIENT_HOST_PATTERN.matcher(value).find()) {
                            hostname = value.split(":")[1].trim();
                        } else if (USER_AGENT_PATTERN.matcher(value).find()) {
                            for (String ua : value.split(":")) {
                                if (!ua.equalsIgnoreCase("User-Agent:")) {
                                    userAgent += ua.trim();
                                }
                            }
                        } else if (ACCEPT_ENCODING_PATTERN.matcher(value).find()) {
                            encoding = value.split(":")[1].trim();
                        }
                    }

                    if (!REQUEST_TYPE.equals("")) {
                        // System.out.println("contentLen ->" + contentLen + "\ncontentType ->" + contentType + "\nhostname ->" + hostname + "\nconnectionType-> " + connectionType + "\nhostname ->" + hostname + "\nuserAgent -> " + userAgent);
                        final String requestLocation = h1[1];
                        if (requestLocation != null) {
                            processLocation(out, requestLocation);
                        }
                        //System.out.println("requestLocation "+requestLocation);
                    }
                }
                socket.close();
            } catch (Exception er) {
                er.printStackTrace();
            }
        }
    }

    public void processLocation(DataOutputStream out, String location) {
        String data = null;

        if (location != "/") {
            System.out.println("url location -> " + location);
            URL getUrl = getDecodedUrl("http://localhost" + location);
            String[] dirPath = getUrl.getPath().split("/");
            String fullFilePath = getUrl.getPath();
            if (dirPath.length > 1) {
                String fileName = dirPath[dirPath.length - 1];
                HashMap<String, String> qParams = splitQuery(getUrl.getQuery());
                //System.out.println("File name " + fileName);
                //System.out.println("url parms " + qparms);
                CONTENT_TYPE = getContentType(fileName);
                if (CONTENT_TYPE.equals("image/jpeg") || CONTENT_TYPE.equals("image/png") || CONTENT_TYPE.equals("video/mp4")) {
                    byte[] bytesData = readImageFiles(WEB_DIR_PATH + fullFilePath, CONTENT_TYPE);
                    //System.out.println(bytdata.length);
                    if (bytesData != null) {
                        constructHeaderImage(out, bytesData.length, bytesData);
                        return;
                    }
                } else {
                    data = readFile(WEB_DIR_PATH + fullFilePath);
                }
            }
        }

        if (data == null || data.trim().equals("")) {
            CONTENT_TYPE = "text/html";
            data = readFile(WEB_DIR_PATH + "/" + INDEX_FILE_NAME);
        }
        constructHeader(out, data.length(), data);
    }

    public URL getDecodedUrl(String parms) {
        try {
            //String decodedurl =URLDecoder.decode(parms,"UTF-8");
            URL aURL = new URL(parms);
            return aURL;
        } catch (Exception er) {
        }
        return null;
    }

    public static HashMap<String, String> splitQuery(String parms) {
        try {
            final HashMap<String, String> query_pairs = new HashMap<>();
            final String[] pairs = parms.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, "");
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.put(key, value);
            }
            return query_pairs;
        } catch (Exception er) {
        }
        return null;
    }

    public void setRequestType(String type) {
        // System.out.println("REQUEST TYPE " + type);
        this.REQUEST_TYPE = type;
    }

    public void setHttpVer(String httpver) {
        // System.out.println("REQUEST ver " + httpver);
        this.HTTP_VER = httpver;
    }

    public String getRequestType() {
        return this.REQUEST_TYPE;
    }

    public String getHttpVer() {
        return this.HTTP_VER;
    }

    //hashtable initilization for content types
    static Hashtable<String, String> mContentTypes = new Hashtable();
    {
        mContentTypes.put("js", "application/javascript");
        mContentTypes.put("php", "text/html");
        mContentTypes.put("java", "text/html");
        mContentTypes.put("json", "application/json");
        mContentTypes.put("png", "image/png");
        mContentTypes.put("jpg", "image/jpeg");
        mContentTypes.put("html", "text/html");
        mContentTypes.put("css", "text/css");
        mContentTypes.put("mp4", "video/mp4");
        mContentTypes.put("mov", "video/quicktime");
        mContentTypes.put("wmv", "video/x-ms-wmv");
    }

    //get request content type
    public static String getContentType(String path) {
        String type = tryGetContentType(path);
        if (type != null) {
            return type;
        }
        return "text/plain";
    }

    //get request content type from path
    public static String tryGetContentType(String path) {
        int index = path.lastIndexOf(".");
        if (index != -1) {
            String e = path.substring(index + 1);
            String ct = mContentTypes.get(e);
            // System.out.println("content type: " + ct);
            if (ct != null) {
                return ct;
            }
        }
        return null;
    }

    private void constructHeader(DataOutputStream output, int size, String data) {
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), false);
        pw.append("HTTP/1.1 ").append(STATUS).append(" \r\n");
        if (this.CONTENT_TYPE != null) {
            printHeader(pw, "Content-Type", this.CONTENT_TYPE);
        }
        printHeader(pw, "Date", gmtFrmt.format(new Date()));
        printHeader(pw, "Connection", (this.keepAlive ? "keep-alive" : "close"));
        printHeader(pw, "Content-Length", String.format("%d", size));
        printHeader(pw, "Server", SERVER_NAME);
        pw.append("\r\n");
        pw.append(data);
        pw.flush();
        //pw.close();
    }

    private void constructHeaderImage(DataOutputStream output, int size, byte[] data) {
        try {
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), false);
            pw.append("HTTP/1.1 ").append(STATUS).append(" \r\n");
            if (this.CONTENT_TYPE != null) {
                printHeader(pw, "Content-Type", this.CONTENT_TYPE);
            }
            printHeader(pw, "Date", gmtFrmt.format(new Date()));
            printHeader(pw, "Connection", (this.keepAlive ? "keep-alive" : "close"));
            printHeader(pw, "Content-Length", String.format("%d", size));
            printHeader(pw, "Server", SERVER_NAME);
            pw.append("\r\n");
            pw.flush();
            output.write(data);
            output.flush();
            //System.out.println("data sent success");

            //pw.close();
        } catch (Exception er) {
            er.printStackTrace();
        }
    }


    @SuppressWarnings("static-method")
    protected void printHeader(PrintWriter pw, String key, String value) {
        pw.append(key).append(": ").append(value).append("\r\n");
    }

    public byte[] readImageFiles(String fileName, String fileType) {
        try {
            File iFile = new File(fileName);
            if (!iFile.exists()) {
                return null;
            }

            if (fileType.equalsIgnoreCase("image/png") || fileType.equalsIgnoreCase("image/jpeg") || fileType.equalsIgnoreCase("image/gif") || fileType.equalsIgnoreCase("image/jpg")) {
                FileInputStream fis = new FileInputStream(fileName);
                byte[] buffer = new byte[fis.available()];
                while (fis.read(buffer) != -1) {
                }
                fis.close();
                return buffer;
            }
        } catch (Exception er) {
            return null;
        }
        return null;
    }

    public String readFile(String fileName) {
        try {
            File iFile = new File(fileName);
            if (iFile.exists()) {
                FileInputStream fis = new FileInputStream(fileName);
                byte[] buffer = new byte[10];
                StringBuilder sb = new StringBuilder();
                while (fis.read(buffer) != -1) {
                    sb.append(new String(buffer));
                    buffer = new byte[10];
                }
                fis.close();
                return sb.toString();
            } else {
                return null;
            }
        } catch (Exception er) {
            return null;
        }
    }

    public static void init(int port, String publicDir) {
        SERVER_PORT = port;
        WEB_DIR_PATH = publicDir;
        scanFileDirectory();
    }

    public static void startServer(int port, String publicDir) {
        try {
            isStart = true;
            init(port, publicDir);
            new TinyWebServer(SERVER_PORT).start();
            Log.d(TAG, "Server Started !");
        } catch (IOException e) {
            Log.d(TAG, "!!!!!!!!!!!");
        } catch (Exception e) {
            Log.d(TAG, "!!!!!!!!!!!");
        }
    }

    public static void stopServer() {
        if (isStart) {
            try {
                isStart = false;
                serverSocket.close();
                System.out.println("Server stopped running !");
            } catch (IOException er) {
                er.printStackTrace();
            }
        }
    }


    //scan for index file
    public static void scanFileDirectory() {
        boolean isIndexFound = false;
        try {
            File file = new File(WEB_DIR_PATH);
            if (file.isDirectory()) {
                File[] allFiles = file.listFiles();
                for (File allFile : allFiles) {
                    //System.out.println(allFile.getName().split("\\.")[0]);
                    if (allFile.getName().split("\\.")[0].equalsIgnoreCase("index")) {
                        TinyWebServer.INDEX_FILE_NAME = allFile.getName();
                        isIndexFound = true;
                    }
                }
            }
        } catch (Exception er) {
            Log.d(TAG, "");
        }

        if (!isIndexFound) {
            System.out.println("Index file not found !");
        }
    }
}