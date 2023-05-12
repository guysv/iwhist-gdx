package guysv.iwhist.android.server;

import guysv.iwhist.server.compat.ServerHost;
import guysv.iwhist.server.compat.ServerHostFactory;

public class AndroidServerHostFactory implements ServerHostFactory {

    @Override
    public ServerHost newHost() {
        return new AndroidServerHost();
    }
}
