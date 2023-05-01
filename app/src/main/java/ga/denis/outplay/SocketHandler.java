package ga.denis.outplay;

import java.net.Socket;

public class SocketHandler {
    private static Socket socket;
    private static String name;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized String getName() {
        return name;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }

    public static synchronized void setName(String name) {
        SocketHandler.name = name;
    }
}
