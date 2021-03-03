package v2.intel;

import hlt.GameMap;
import hlt.Move;
import hlt.Planet;
import hlt.Ship;

import java.util.List;

public class DockTask extends Task {
    private final double THREAT_DISTANCE = 35d;

    public DockTask(Planet planet) {
        super();

        this.moveType = Move.MoveType.Dock;
        this.targetPlanet = planet;
    }

    @Override
    public boolean canBeCompleted(Ship ship, GameMap gameMap) {
        final Planet planet = gameMap.getPlanet(targetPlanet.getId());
        if (planet == null) {
            return false;
        }

        if (planet.getOwner() > -1 && planet.getOwner() != gameMap.getMyPlayerId()) {
            return false;
        }

        // if there's an enemy ship near the planet, docking is not possible
        final List<Ship> ships = gameMap.getAllShips();
        final int PLAYER_ID = gameMap.getMyPlayerId();
        for (Ship s : ships) {
            if (s.getOwner() == PLAYER_ID) {
                continue;
            }

            if (s.getDistanceTo(planet) < THREAT_DISTANCE) {
                return false;
            }
        }

        if (!planet.isOwned()) {
            return true;
        }

        if (planet.getDockingSpots() > planet.getDockedShips().size()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isComplete(Ship ship, GameMap gameMap) {
        return ship.getDockingStatus() == Ship.DockingStatus.Docked;
    }
}
