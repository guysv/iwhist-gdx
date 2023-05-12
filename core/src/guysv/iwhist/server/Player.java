package guysv.iwhist.server;

import java.util.UUID;

public class Player {
    private String auth;
    private int id;
    private String name;
    private boolean ready;

    public Player(int id, String auth, String name) {
        this.auth = auth;
        this.id = id;
        this.name = name;
    }

    public String getAuthToken() {
        return auth;
    }

    public int getId() {
        return id;
    }

    public boolean isReady() {
        return ready;
    }

    // return previous value (aready ready?)
    public boolean setReady() {
        boolean prev = ready;
        ready = true;
        return prev;
    }
}
