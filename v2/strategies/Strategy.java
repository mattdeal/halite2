package v2.strategies;

import hlt.GameMap;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.GameInfo;

public abstract class Strategy {
    private final int id;
    protected boolean active;

    final int PLAYER_ID;

    public Strategy (final int id, GameMap gameMap) {
        this.id = id;
        this.active = true;
        this.PLAYER_ID = gameMap.getMyPlayerId();
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    // generate an assignment for this ship
    public abstract Assignment generateAssignment(Ship ship, GameMap gameMap);

    // release any holds on the resources reserved by this assignment
    public abstract void releaseHolds(Assignment assignment);

    // activate/deactivate based on intel
    // release lost ships, planets, players
    public abstract void update(GameInfo intel);
}
