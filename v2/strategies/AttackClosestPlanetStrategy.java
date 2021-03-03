package v2.strategies;

import hlt.GameMap;
import hlt.Log;
import hlt.Planet;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.AttackTask;
import v2.intel.GameInfo;

import java.util.Map;
import java.util.TreeMap;

public class AttackClosestPlanetStrategy extends Strategy {
    final int MAX_SHIPS_PER_TARGET = 10;

    Map<Integer, Integer> reservedShips; // shipId, # of ships assigned to attack this target

    public AttackClosestPlanetStrategy(int id, GameMap gameMap) {
        super(id, gameMap);

        reservedShips = new TreeMap<>();
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        final Map<Double, Planet> planets = new TreeMap<>();
        for (final Planet planet : gameMap.getAllPlanets().values()) {
            final double distanceToPlanet = ship.getDistanceTo(planet);
            if (planet.isOwned() && planet.getOwner() != PLAYER_ID) {
                planets.put(distanceToPlanet, planet);
            }
        }

        for (final Planet planet : planets.values()) {
            // sort docked ships by distance
            final Map<Double, Ship> targets = new TreeMap<>();
            for (int targetId : planet.getDockedShips()) {
                final Ship targetShip = gameMap.getShip(planet.getOwner(), targetId);

                // ignore ships we've already targeted
                if (reservedShips.containsKey(targetId) && reservedShips.get(targetId) > MAX_SHIPS_PER_TARGET) {
                    continue;
                }

                if (targetShip != null) {
                    targets.put(ship.getDistanceTo(targetShip), targetShip);
                }
            }

            // create attack assignment for the closest docked ship
            for (final Ship targetShip : targets.values()) {
                Assignment assignment = new Assignment();
                assignment.setStrategyId(this.getId());
                assignment.setShipId(ship.getId());
                assignment.setGoal(new AttackTask(targetShip, null));

                // reserve ship
                if (reservedShips.containsKey(targetShip.getId())) {
                    final int assignedCount = reservedShips.get(targetShip.getId()) + 1;
                    reservedShips.put(targetShip.getId(), assignedCount);
                } else {
                    reservedShips.put(targetShip.getId(), 1);
                }

                Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " attack ship " + targetShip.getId());

                return assignment;
            }
        }

        Log.log("Strategy " + getId() + " Ship " + ship.getId() + " was not given an assignment.");

        return null;
    }

    @Override
    public void releaseHolds(Assignment assignment) {

    }

    @Override
    public void update(GameInfo intel) {

    }
}
