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

public class MaximizeProductionStrategy extends Strategy {
    private final double MAX_TRAVEL_DISTANCE = 19d; // max distance a ship should travel to max production

    private Map<Integer, Integer> shipAssignments; // planetId, # reserved Ships - used to track how many ships have been assigned


    public MaximizeProductionStrategy(int id, GameMap gameMap) {
        super(id, gameMap);

        shipAssignments = new TreeMap<>();
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        // get owned planets within MAX_TRAVEL_DISTANCE of this ship
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

        for (Planet planet : planetsInRange.values()) {
            final int missingProduction = planet.getDockingSpots() - planet.getDockedShips().size();

            Log.log("Strategy " + getId() + " planet " + planet.getId() + " spots " + planet.getDockingSpots() + " docked " + planet.getDockedShips().size());

            // production is maxed
            if (missingProduction < 1) {
                Log.log("Strategy " + getId() + " planet " + planet.getId() + " has max production");
                continue;
            }

            // enough ships have been assigned to this planet
            if (shipAssignments.containsKey(planet.getId()) && shipAssignments.get(planet.getId()) >= missingProduction) {
                Log.log("Strategy " + getId() + " planet " + planet.getId() + " has missing production " + missingProduction + " and " + shipAssignments.get(planet.getId()) + " ships assigned");
                continue;
            }

            Assignment assignment = new Assignment();
            assignment.setStrategyId(this.getId());
            assignment.setShipId(ship.getId());
            assignment.setGoal(new DockTask(planet));

            reservePlanet(planet.getId());

            Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId());

            return assignment;
        }

        Log.log("Strategy " + getId() + " Ship " + ship.getId() + " was not given an assignment.");

        return null;
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        final int planetId = assignment.getGoal().getTargetPlanet().getId();
        if (shipAssignments.containsKey(planetId) && shipAssignments.get(planetId) > 0) {
            shipAssignments.put(planetId, shipAssignments.get(planetId) - 1);
        }
    }

    @Override
    public void update(GameInfo intel) {

    }

    private void reservePlanet(int planetId) {
        if (shipAssignments.containsKey(planetId)) {
            shipAssignments.put(planetId, shipAssignments.get(planetId) + 1);
        } else {
            shipAssignments.put(planetId, 1);
        }
    }
}
