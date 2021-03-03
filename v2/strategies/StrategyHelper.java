package v2.strategies;

import hlt.GameMap;
import hlt.Log;
import hlt.Player;
import hlt.Ship;

import v2.intel.GameInfo;

import java.util.Map;
import java.util.TreeMap;

public class StrategyHelper {
    private static final int STRATEGY_ID_INCREMENT = 10;
    private static final double RUSH_MAX_DISTANCE = 86d;
    private static final double MIN_DEFENSE_DISTANCE = 21d;
    private static final double MAX_DEFENSE_DISTANCE = 42d;

    private static int curStrategyId = 0;

    public StrategyHelper() {}

    public Map<Integer,Strategy> createStrategies(GameInfo intel) {
        final Map<Integer, Strategy> strategies = new TreeMap<>();
        final GameMap gameMap = intel.getGamemap();

        // 1v1 rush strategy
        if (shouldRush(intel)) {
            strategies.put(curStrategyId, new AttackClosestShipStrategy(curStrategyId, gameMap));
            curStrategyId += STRATEGY_ID_INCREMENT;
        }

        // add initial expansion strategy
        strategies.put(curStrategyId, new InitialExpansionStrategy(curStrategyId, gameMap));
        curStrategyId += STRATEGY_ID_INCREMENT;

        // add close defensive strategy
        strategies.put(curStrategyId, new DefendPlanetStrategy(curStrategyId, gameMap, MIN_DEFENSE_DISTANCE));
        curStrategyId += STRATEGY_ID_INCREMENT;

        // maximize production strategy
        strategies.put(curStrategyId, new MaximizeProductionStrategy(curStrategyId, gameMap));
        curStrategyId += STRATEGY_ID_INCREMENT;

        // add further defensive strategy
//        strategies.put(curStrategyId, new DefendPlanetStrategy(curStrategyId, gameMap, MAX_DEFENSE_DISTANCE));
//        curStrategyId += STRATEGY_ID_INCREMENT;

        // expansion strategy
        strategies.put(curStrategyId, new LateExpansionStrategy(curStrategyId, gameMap));
        curStrategyId += STRATEGY_ID_INCREMENT;

        // expansion strategy
//        strategies.put(curStrategyId, new ExpansionStrategy(curStrategyId, gameMap));
//        curStrategyId += STRATEGY_ID_INCREMENT;

        // ffa hide in corner strategy
        if (shouldHideInCorner(intel)) {
            strategies.put(curStrategyId, new HideInCornerStrategy(curStrategyId, gameMap));
            curStrategyId += STRATEGY_ID_INCREMENT;
        }

        // attack closest planet
//        strategies.put(curStrategyId, new AttackClosestPlanetStrategy(curStrategyId, gameMap));
//        curStrategyId += STRATEGY_ID_INCREMENT;

        // attack closest ship strategy
//        strategies.put(curStrategyId, new AttackClosestShipStrategy(curStrategyId, gameMap));
//        curStrategyId += STRATEGY_ID_INCREMENT;

        // attack closest ship strategy
        strategies.put(curStrategyId, new WaitToAttackClosestShipStrategy(curStrategyId, gameMap));
        curStrategyId += STRATEGY_ID_INCREMENT;

        // todo: other

        return strategies;
    }

    // determine if we should rush the other player in a 1v1 match
    private boolean shouldRush(GameInfo intel) {
        if (intel.getNumPlayers() > 2) {
            return false;
        }

        final GameMap gameMap = intel.getGamemap();
        final Map<Integer, Ship> myShips = gameMap.getMyPlayer().getShips();

        // get enemy player ships
        for (Player player : gameMap.getAllPlayers()) {
            if (player.getId() != gameMap.getMyPlayerId()) {
                for (Ship ship : player.getShips().values()) {
                    for (Ship myShip : myShips.values()) {
                        if (myShip.getDistanceTo(ship) < RUSH_MAX_DISTANCE) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // determine if we should hide a ship in the corner in a ffa match
    private boolean shouldHideInCorner(GameInfo intel) {
        return intel.getNumPlayers() > 2;
    }
}
