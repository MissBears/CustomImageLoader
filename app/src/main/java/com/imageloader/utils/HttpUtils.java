package com.imageloader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wangzhiguo on 15/9/1.
 */
public class HttpUtils {
    /**
     *
     * @param url
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream download(String url) throws MalformedURLException,IOException{
        HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        return conn.getInputStream();
    }
}
