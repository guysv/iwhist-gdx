package guysv.iwhist.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import guysv.iwhist.server.event.EventType;
import guysv.iwhist.server.event.ServerEvent;
import guysv.iwhist.server.exception.AlreadyReadyException;
import guysv.iwhist.server.exception.AuthException;
import guysv.iwhist.server.exception.GameAlreadyInProgressException;
import guysv.iwhist.server.exception.RoomFullException;

public class GameServer {
    private BlockingQueue<ServerEvent> eventQueue = new LinkedBlockingQueue<>();
    private List<Player> players = new ArrayList<>(4);
    private GameState gameState = new GameState();

    public ServerEvent waitEvent() throws InterruptedException {
        return eventQueue.take();
    }

    public String signup(String name) throws RoomFullException, GameAlreadyInProgressException {
        validateNotInProgress();

        Player player = new Player(players.size(), IdToken.generateToken(), name);

        try {
            players.add(player);
        } catch (IllegalStateException e) {
            throw new RoomFullException();
        }

        eventQueue.add(new ServerEvent.Builder(EventType.PLAYER_SIGNUP)
                .player(player.getId())
                .build());

        return player.getAuthToken();
    }

    public void ready(String authToken) throws AuthException, GameAlreadyInProgressException,
            AlreadyReadyException {
        validateNotInProgress();
        Player player = authenticate(authToken);

        if (player.setReady()) {
            throw new AlreadyReadyException();
        }

        eventQueue.add(new ServerEvent.Builder(EventType.PLAYER_READY)
                .player(player.getId())
                .build());
    }

    public void status() {

    }

    public void reset() {
        eventQueue.add(new ServerEvent.Builder(EventType.GAME_RESET)
                .build());

        players.clear();
        gameState.reset();
    }

    private void validateNotInProgress() throws GameAlreadyInProgressException {
        if (gameState.isGameInProgress()) {
            throw new GameAlreadyInProgressException();
        }
    }

    private Player authenticate(String authToken) throws AuthException {
        for (Player p : players) {
            if (p.getAuthToken().equals(authToken)) {
                return p;
            }
        }

        throw new AuthException();
    }
}
