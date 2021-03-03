package v2.strategies;

import hlt.GameMap;
import hlt.Log;
import hlt.Planet;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.DockTask;
import v2.intel.GameInfo;

import java.util.Map;
import java.util.TreeMap;

public class InitialExpansionStrategy extends ExpansionStrategy {
    protected Map<Integer, Integer> reservations; // planetId, reservedShipCount

    public InitialExpansionStrategy(int id, GameMap gameMap) {
        super(id, gameMap);
        reservations = new TreeMap<>();
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        return;
    }

    @Override
    public void update(GameInfo intel) {
        // deactivate this Strategy after the first turn
        if (intel.getTurnNumber() > 1) {
            this.active = false;
        }
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        final Map<Double, Planet> planetsByDistance = new TreeMap<>();

        for (final Planet planet : gameMap.getAllPlanets().values()) {
            final double distanceToPlanet = ship.getDistanceTo(planet);
            planetsByDistance.put(distanceToPlanet, planet);
        }

        for (Planet planet : planetsByDistance.values()) {
            // ignore owned planets
            if (planet.isOwned()) {
                continue;
            }

            // ignore planets that are already maxed out
            if (reservations.containsKey(planet.getId()) && reservations.get(planet.getId()) >= planet.getDockingSpots()) {
                continue;
            }

            // create assignment
            Assignment assignment = new Assignment();
            assignment.setStrategyId(this.getId());
            assignment.setShipId(ship.getId());
            assignment.setGoal(new DockTask(planet));

            // if assignment can be completed, we're done
            if (assignment.canBeCompleted(ship, gameMap)) {
                reservePlanet(planet);

                Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " dock on planet " + planet.getId());
                return assignment;
            }

            Log.log("Strategy " + getId() + " ship " + ship.getId() + " could not complete assignment (dock on planet " + planet.getId() + ")");
        }

        Log.log("Strategy " + getId() + " Ship " + ship.getId() + " was not given an assignment.");

        return null;
    }

    private void reservePlanet(Planet planet) {
        if (reservations.containsKey(planet.getId())) {
            final int reserved = reservations.get(planet.getId()) + 1;
            reservations.put(planet.getId(), reserved);
        } else {
            reservations.put(planet.getId(), 1);
        }
    }
}
