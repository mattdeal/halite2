package v2.bots;

import hlt.GameMap;
import hlt.Networking;

public abstract class Bot {
    GameMap gameMap;
    Networking networking;

    public Bot(Networking networking, GameMap gameMap) {
        this.networking = networking;
        this.gameMap = gameMap;
    }

    public abstract void run();
}
