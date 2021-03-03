package v2.strategies;

import hlt.GameMap;
import v2.intel.GameInfo;

public class WaitToAttackClosestShipStrategy extends AttackClosestShipStrategy {
    private final int WAIT_UNTIL = 50;

    public WaitToAttackClosestShipStrategy(int id, GameMap gameMap) {
        super(id, gameMap);
    }

    @Override
    public void update(GameInfo intel) {
        if (intel.getTurnNumber() < WAIT_UNTIL) {
            this.active = false;
        } else {
            this.active = true;
        }
    }
}
