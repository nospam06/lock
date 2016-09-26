package com.ohmyapp.lock.pojo;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Emerald on 9/24/2016.
 * connection
 */
public class SocketConnection {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * connection cache
     *
     * @param inSocket socket
     * @param inWriter writer
     * @param inReader reader
     */
    public SocketConnection(Socket inSocket, PrintWriter inWriter, BufferedReader inReader) {
        socket = inSocket;
        writer = inWriter;
        reader = inReader;
    }

    /**
     * @return socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @return writer
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * @return reader
     */
    public BufferedReader getReader() {
        return reader;
    }
}
