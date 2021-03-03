package v2.bots;

import hlt.*;

import v2.intel.Assignment;
import v2.intel.GameInfo;

import v2.nav.Navigation;

import v2.strategies.Strategy;
import v2.strategies.StrategyHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StrategyBot extends Bot {

    Map<Integer, Strategy> strategies; // strategyId, Strategy - used to access/order strategies
    GameInfo intel;
    StrategyHelper helper;
    Navigation nav;

    public StrategyBot(Networking networking, GameMap gameMap) {
        super(networking, gameMap);

        this.intel = new GameInfo(gameMap);
        this.helper = new StrategyHelper();
        this.strategies = helper.createStrategies(this.intel);
        this.nav = new Navigation();
    }

    @Override
    public void run() {
        long startTime = 0l;

        for (;;) {
            startTime = System.nanoTime();
            networking.updateMap(gameMap);
            intel.update(gameMap);
            updateStrategies();

            // update missing ship assignments/strategies
            final Set<Integer> missingShips = intel.getMissingShipIds();
            for (int missingId : missingShips) {
                final Assignment missingAssignment = intel.getAssignment(missingId);
                if (missingAssignment != null) {
                    strategies.get(missingAssignment.getStrategyId()).releaseHolds(missingAssignment);
                }
            }

            // remove missing ships from intel
            if (missingShips != null) {
                intel.removeShips(missingShips);
            }

            // generate assignments/moves for each ship
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                // ignore docked ships
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    Log.log("Ship " + ship.getId() + " is docked on planet " + ship.getDockedPlanet());
                    continue;
                }

                final int shipId = ship.getId();
                final Assignment assignment = intel.getAssignment(shipId);
                boolean needsAssignment = false;

                // determine if ship can finish it's assignment
                if (assignment != null) {
                    if (assignment.isComplete(ship, gameMap)) {
                        Log.log("Ship " + ship.getId() + " completed assignment");
                        strategies.get(assignment.getStrategyId()).releaseHolds(assignment);
                        intel.setAssignment(shipId, null);
                        needsAssignment = true;
                    } else if (!assignment.canBeCompleted(ship, gameMap)) {
                        Log.log("Ship " + ship.getId() + " cannot complete assignment");
                        strategies.get(assignment.getStrategyId()).releaseHolds(assignment);
                        intel.setAssignment(shipId, null);
                        needsAssignment = true;
                    }
                }

                // ship has no assignment, or it was cleared, give it a new assignment
                if (needsAssignment || assignment == null) {
                    for (Strategy strategy : strategies.values()) {
                        Log.log("Strategy " + strategy.getId() + " active=" + strategy.isActive());
                        if (strategy.isActive()) {
                            final Assignment newAssignment = strategy.generateAssignment(ship, gameMap);
                            if (newAssignment != null) {
                                Log.log("Strategy " + strategy.getId() + " assigned ship " + shipId);
                                intel.setAssignment(shipId, newAssignment);
                                break;
                            }
                        }
                    }
                }

                // generate move based on assignment
                final Assignment assignmentForMove = intel.getAssignment(shipId);
                if (assignmentForMove != null) {
                    Move move = null;

                    // check for target ship, planet, position
                    if (assignmentForMove.getGoal().getTargetShip() != null) {
                        // get the actual location of ths ship
                        final Ship targetShip = assignmentForMove.getGoal().getTargetShip();
                        final Ship realTargetShip = gameMap.getShip(targetShip.getOwner(), targetShip.getId());
                        final Position targetPosition = ship.getClosestPoint(realTargetShip);

                        move = nav.navigateShipTowardsTarget(gameMap, ship, targetPosition, Constants.MAX_SPEED, intel.getShipPaths());
                    } else if (assignmentForMove.getGoal().getTargetPlanet() != null) {
                        if (assignmentForMove.getGoal().getMoveType() == Move.MoveType.Dock) {
                            move = nav.navigateShipToDock(gameMap, ship, assignmentForMove.getGoal().getTargetPlanet(), intel.getShipPaths());
                        } else {
                            move = nav.navigateShipTowardsTarget(gameMap, ship, assignmentForMove.getGoal().getTargetPlanet(), Constants.MAX_SPEED, intel.getShipPaths());
                        }
                    } else if (assignmentForMove.getGoal().getTargetPosition() != null) {
                        move = nav.navigateShipTowardsTarget(gameMap, ship, assignmentForMove.getGoal().getTargetPosition(), Constants.MAX_SPEED, intel.getShipPaths());
                    }

                    if (move != null) {
                        intel.addMove(move);
                    } else {
                        // ship is stuck or something, reset it next turn
                        strategies.get(assignmentForMove.getStrategyId()).releaseHolds(assignmentForMove);
                        intel.setAssignment(shipId, null);
                    }
                }

                // mark ship as seen
                intel.updateLastSeen(shipId);

                // break if time is about to expire
//                    if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) > 1800) {
//                        Log.log("Time = " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " broke out");
//                        break;
//                    }
            }

            // send moves
            networking.sendMoves(intel.getMoveList());

            Log.log("Time = " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

//            try {
//
//            } catch (Exception ex) {
//                Log.log("Time = " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
//                Log.log(ex.toString());
//
//                StringWriter writer = new StringWriter();
//                PrintWriter printWriter = new PrintWriter( writer );
//                ex.printStackTrace( printWriter );
//                printWriter.flush();
//
//                String stackTrace = writer.toString();
//
//                Log.log(stackTrace);
//
////                networking.sendMoves(intel.getMoveList());
//            }
        }
    }

    private void updateStrategies() {
        for (Strategy strategy : strategies.values()) {
            strategy.update(intel);
        }
    }
}
