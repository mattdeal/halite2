package v2.strategies;

import hlt.GameMap;
import v2.intel.Assignment;
import v2.intel.GameInfo;

public class LateExpansionStrategy extends InitialExpansionStrategy {
    public LateExpansionStrategy(int id, GameMap gameMap) {
        super(id, gameMap);
    }

    @Override
    public void update(GameInfo intel) {
        return;
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        final int planetId = assignment.getGoal().getTargetPlanet().getId();
        final int reserved = reservations.get(planetId) - 1;
        reservations.put(planetId, reserved);
    }
}
