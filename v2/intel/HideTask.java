package v2.intel;

import hlt.GameMap;
import hlt.Position;
import hlt.Ship;

public class HideTask extends Task {
    public HideTask(Position position) {
        super();
        this.targetPosition = position;
    }

    @Override
    public boolean canBeCompleted(Ship ship, GameMap gameMap) {
        // can always be completed
        return true;
    }

    @Override
    public boolean isComplete(Ship ship, GameMap gameMap) {
        // always false
        return false;
    }
}
