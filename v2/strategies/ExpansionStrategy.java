package v2.strategies;

import hlt.GameMap;
import hlt.Log;
import hlt.Planet;
import hlt.Ship;

import v2.intel.Assignment;
import v2.intel.DockTask;
import v2.intel.GameInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExpansionStrategy extends Strategy {
    private final double MAX_TRAVEL_DISTANCE;

    private Set<Integer> reservedPlanetIds;

    public ExpansionStrategy(int id, GameMap gameMap) {
        super(id, gameMap);

        reservedPlanetIds = new HashSet<>();

        MAX_TRAVEL_DISTANCE = gameMap.getWidth() / 4;
    }

    @Override
    public Assignment generateAssignment(Ship ship, GameMap gameMap) {
        final Map<Double, Planet> planetsByDistance = new TreeMap<>();

        for (final Planet planet : gameMap.getAllPlanets().values()) {
            final double distanceToPlanet = ship.getDistanceTo(planet);
            if (!planet.isOwned() && !reservedPlanetIds.contains(planet.getId()) && distanceToPlanet < MAX_TRAVEL_DISTANCE) {
                planetsByDistance.put(distanceToPlanet, planet);
            }
        }

        for (Planet planet : planetsByDistance.values()) {
            // ignore owned planets
            if (planet.isOwned()) {
                continue;
            }

            // planet is reserved
            if (reservedPlanetIds.contains(planet.getId())) {
                continue;
            }

            // create assignment
            Assignment assignment = new Assignment();
            assignment.setStrategyId(this.getId());
            assignment.setShipId(ship.getId());
            assignment.setGoal(new DockTask(planet));

            // if assignment can be completed, we're done
            if (assignment.canBeCompleted(ship, gameMap)) {
                Log.log("Strategy " + getId() + " Created Assignment for ship " + ship.getId() + " dock on planet " + planet.getId());

                reservePlanet(planet.getId());

                return assignment;
            }

            Log.log("Strategy " + getId() + " ship " + ship.getId() + " could not complete assignment (dock on planet " + planet.getId() + ")");
        }

        Log.log("Strategy " + getId() + " Ship " + ship.getId() + " was not given an assignment.");

        return null;
    }

    @Override
    public void releaseHolds(Assignment assignment) {
        releasePlanet(assignment.getGoal().getTargetPlanet().getId());
    }

    @Override
    public void update(GameInfo intel) {
        // not implemented for this Strategy
    }

    private void releasePlanet(int planetId) {
        reservedPlanetIds.remove(planetId);
        Log.log("Strategy " + getId() + " Released planet " + planetId);
    }

    private void reservePlanet(int planetId) {
        reservedPlanetIds.add(planetId);
        Log.log("Strategy " + getId() + " Reserved planet " + planetId);
    }
}
