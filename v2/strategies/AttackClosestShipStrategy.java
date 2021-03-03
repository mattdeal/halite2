package v2.strategies;

import hlt.Entity;
import hlt.GameMap;
import hlt.Log;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.AttackTask;
import v2.intel.GameInfo;

import java.util.Map;

public class AttackClosestShipStrategy extends Strategy {

    public AttackClosestShipStrategy(int id, GameMap gameMap) {
        super(id, gameMap);
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        // find nearest enemy ship and fly to it
        Map<Double, Entity> nearby = gameMap.nearbyEntitiesByDistance(ship);
        for (Entity entity : nearby.values()) {
            if (entity instanceof Ship) {
                if (entity.getOwner() != PLAYER_ID) {
                    Assignment assignment = new Assignment();
                    assignment.setStrategyId(this.getId());
                    assignment.setShipId(ship.getId());
                    assignment.setGoal(new AttackTask((Ship)entity, null));

                    Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " attack ship " + entity.getId());

                    return assignment;
                }
            }
        }

        return null;
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        // not implemented for this Strategy
    }

    @Override
    public void update(GameInfo intel) {
        // not implemented for this Strategy
    }
}
