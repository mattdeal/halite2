package v2.strategies;

import hlt.GameMap;
import hlt.Log;
import hlt.Position;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.GameInfo;
import v2.intel.HideTask;

import java.util.HashSet;
import java.util.Set;

public class HideInCornerStrategy extends Strategy {
    private final double MAX_X;
    private final double MAX_Y;
    private final double MID_X;
    private final double MID_Y;

    private Set<Integer> reservedShipIds = new HashSet<>();

    public HideInCornerStrategy(int id, GameMap gameMap) {
        super(id, gameMap);

        this.MAX_X = gameMap.getWidth();
        this.MAX_Y = gameMap.getHeight();
        this.MID_X = MAX_X / 2d;
        this.MID_Y = MAX_Y / 2d;
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        if (reservedShipIds.size() > 0) {
            return null;
        }

        final double x = ship.getXPos();
        final double y = ship.getYPos();
        double newX, newY;

        if (x > MID_X) {
            newX = MAX_X;

            if (y > MID_Y) {
                newY = MAX_Y;
            } else {
                newY = 0d;
            }
        } else {
            newX = 0d;

            if (y > MID_Y) {
                newY = MAX_Y;
            } else {
                newY = 0d;
            }
        }

        final Position position = new Position(newX, newY);

        Assignment assignment = new Assignment();
        assignment.setStrategyId(this.getId());
        assignment.setShipId(ship.getId());
        assignment.setGoal(new HideTask(position));

        reservedShipIds.add(ship.getId());

        Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " hide at " + position);

        return assignment;
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        reservedShipIds.remove(assignment.getShipId());
    }

    @Override
    public void update(GameInfo intel) {
        // do nothing
    }
}
