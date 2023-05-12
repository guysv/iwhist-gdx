package guysv.iwhist.android.server;

import android.util.Log;

import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServer;

import guysv.iwhist.server.GameServer;
import guysv.iwhist.server.compat.ServerHost;
import guysv.iwhist.server.exception.AlreadyReadyException;
import guysv.iwhist.server.exception.AuthException;
import guysv.iwhist.server.exception.GameAlreadyInProgressException;
import guysv.iwhist.server.exception.RoomFullException;

public class AndroidServerHost implements ServerHost {
    private AsyncHttpServer httpServer;
    private GameServer gameServer;

    @Override
    public void startServer() {
        httpServer = new AsyncHttpServer();

        httpServer.websocket("/events", (webSocket, request) -> {
            webSocket.setWriteableCallback(() -> {
                try {
                    webSocket.send(new Gson().toJson(gameServer.waitEvent()));
                } catch (InterruptedException e) {
                    webSocket.close();
                }
            });
        });

        httpServer.get("/signup", (request, response) -> {
            try {
                String token = gameServer.signup(request.getQuery().getString("name"));
                response.code(200);
                response.send(token);
            } catch (RoomFullException | GameAlreadyInProgressException e) {
                response.code(400);
                response.send(e.toString());
            }
        });

        httpServer.get("/ready", (request, response) -> {
            try {
                gameServer.ready(request.getHeaders().get("X-Auth-Token"));
                response.code(200);
            } catch (AuthException | GameAlreadyInProgressException | AlreadyReadyException e) {
                response.code(400);
                response.send(e.toString());
            }
        });


        httpServer.listen(56689); // Set the desired port number

        Log.i("GAMESERVER", "Server started on port 56689");
    }

    @Override
    public void stopServer() {
        httpServer.stop();
    }

}
