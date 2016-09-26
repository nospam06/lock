package com.ohmyapp.lock.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Emerald on 9/20/2016.
 * utils
 */
public class LockUtils {
    private LockUtils() {
        // static methods only
    }

    /**
     * @param socket socket
     * @return buffered reader
     * @throws IOException ioe
     */
    public static BufferedReader createReader(Socket socket) throws IOException {
        InputStream stream = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * @param socket socket
     * @return print writer
     * @throws IOException ioe
     */
    public static PrintWriter createWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }
}
