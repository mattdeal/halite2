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

public class DefendPlanetStrategy extends Strategy {
    private final double MAX_TRAVEL_DISTANCE;
    private final double THREAT_DISTANCE;

    public DefendPlanetStrategy(int id, GameMap gameMap, double threatDistance) {
        super(id, gameMap);

        this.THREAT_DISTANCE = threatDistance;
        this.MAX_TRAVEL_DISTANCE = threatDistance;
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        // get owned planets within MAX_DISTANCE of this ship
        final Map<Double, Planet> planetsInRange = new TreeMap<>();
        for (final Planet planet : gameMap.getAllPlanets().values()) {
            final double distanceToPlanet = ship.getDistanceTo(planet);
            if (planet.getOwner() == PLAYER_ID && distanceToPlanet < MAX_TRAVEL_DISTANCE) {
                planetsInRange.put(distanceToPlanet, planet);
            }
        }

        // no planets in range
        if (planetsInRange.size() < 1) {
            return null;
        }

        // have planets in range (distance, planet)
        // iterate over all enemy ships and determine if they're too close to each planet
        // get enemy ships near owned planets
        final Map<Double, Ship> enemyShips = new TreeMap<>();
        for (final Ship oShip : gameMap.getAllShips()) {
            // ignore my ships
            if (oShip.getOwner() == PLAYER_ID) {
                continue;
            }

            // determine if any enemies are close to this ships closest planets
            for (final Planet planet : planetsInRange.values()) {
                final double distanceToPlanet = oShip.getDistanceTo(planet);
                if (distanceToPlanet < THREAT_DISTANCE) {
                    enemyShips.put(distanceToPlanet, oShip);
                }
            }

            // stop looking for targets, at least one has been found on a planet that is close
            if (enemyShips.size() > 0) {
                break;
            }
        }

        // if we found a target, create an attack assignment
        for (final Ship targetShip : enemyShips.values()) {
            Assignment assignment = new Assignment();
            assignment.setStrategyId(this.getId());
            assignment.setShipId(ship.getId());
            assignment.setGoal(new AttackTask(targetShip, null));

            Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " attack ship " + targetShip.getId() + " (p " + targetShip.getOwner() + ")");
            Log.log("My Ship = " + ship.toString());

            return assignment;
        }

        // assign attack orders for enemy ships near a planet
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
