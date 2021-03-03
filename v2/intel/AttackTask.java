package v2.intel;

import hlt.GameMap;
import hlt.Planet;
import hlt.Position;
import hlt.Ship;

public class AttackTask extends Task {

    public AttackTask(Ship targetShip, Position targetPosition) {
        this.targetShip = targetShip;
        this.targetPosition = targetPosition;
    }

    @Override
    public boolean canBeCompleted(Ship ship, GameMap gameMap) {
        final Ship s = gameMap.getShip(targetShip.getOwner(), targetShip.getId());
        return s != null && s.getHealth() > 0;
    }

    @Override
    public boolean isComplete(Ship ship, GameMap gameMap) {
        final Ship s = gameMap.getShip(targetShip.getOwner(), targetShip.getId());
        final boolean shipIsDead = (s == null || s.getHealth() < 1);

        // if the ship is dead, but it was docked and the planet is still controlled, reassign
        if (shipIsDead && targetShip.getDockedPlanet() > -1 && gameMap.getPlanet(targetShip.getDockedPlanet()) != null) {
            final Planet planet = gameMap.getPlanet(targetShip.getDockedPlanet());
            if (planet.getOwner() != gameMap.getMyPlayerId() && planet.getOwner() > -1) {
                // assign new ship
                for (int shipId : planet.getDockedShips()) {
                    this.targetShip = gameMap.getShip(planet.getOwner(), shipId);
                    this.targetPosition = ship.getClosestPoint(this.targetShip);
                    return false;
                }
            }
        }

        return shipIsDead;
    }
}
