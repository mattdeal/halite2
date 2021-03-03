package v2.intel;

import hlt.*;
import v2.nav.Path;

import java.util.*;

public class GameInfo {
    private GameMap gamemap;

    private final int numPlayers; // number of players at start of game
    private final int numPlanets; // number of planets at start of game

    private int curPlayers;
    private int curPlanets;
    private int turnNumber;

    private boolean planetDestroyed;
    private boolean shipsLost;
    private boolean playerDefeated;

    // todo: additional info for things like gaining/losing control of planets, weakening/strengthening opponents, etc.

    private ArrayList<Move> moveList; // list of moves to be submitted this turn
    private Map<Integer, Assignment> assignments; // shipId, Assignment - map of ships and their assigned tasks
    private Map<Integer, Integer> lastSeen; // shipId, turnNumber - map of the last time a ship was seen
    private Map<Integer, Path> shipPath; // shipId, Path - map of the path a ship will take this turn

    public GameInfo(GameMap gamemap) {
        this.gamemap = gamemap;
        this.numPlayers = gamemap.getAllPlayers().size();
        this.numPlanets = gamemap.getAllPlanets().size();
        this.curPlanets = numPlanets;
        this.curPlayers = numPlayers;
        this.turnNumber = 0;
        this.moveList = new ArrayList<>();
        this.assignments = new TreeMap<>();
        this.lastSeen = new TreeMap<>();
        this.shipPath = new TreeMap<>();
    }

    public void update(GameMap gamemap) {
        this.gamemap = gamemap;
        updateIntel();
    }

    private void updateIntel() {
        // todo: player count
        // todo: planet count

        planetDestroyed = false;
        shipsLost = false;

        // todo: if events happened, planet destroyed, ships lost, etc, set boolean appropriately

        moveList.clear();
        shipPath.clear();

        turnNumber++;
    }

    public Set<Integer> getMissingShipIds() {
        final int threshold = turnNumber - 1;
        final Set<Integer> result = new HashSet<>();

        for (int shipId : lastSeen.keySet()) {
            if (lastSeen.get(shipId) < threshold) {
                result.add(shipId);
            }
        }

        return result;
    }

    public void removeShips(Set<Integer> shipIds) {
        for (int shipId : shipIds) {
            lastSeen.remove(shipId);
            assignments.remove(shipId);
        }
    }

    public GameMap getGamemap() {
        return gamemap;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getNumPlanets() {
        return numPlanets;
    }

    public int getCurPlayers() {
        return curPlayers;
    }

    public int getCurPlanets() {
        return curPlanets;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isPlanetDestroyed() {
        return planetDestroyed;
    }

    public boolean isShipsLost() {
        return shipsLost;
    }

    public boolean isPlayerDefeated() {
        return playerDefeated;
    }

    public ArrayList<Move> getMoveList() {
        return moveList;
    }

    public Map<Integer, Assignment> getAssignments() {
        return assignments;
    }

    public Map<Integer, Integer> getLastSeen() {
        return lastSeen;
    }

    public Assignment getAssignment(int shipId) {
        if (assignments.containsKey(shipId)) {
            return assignments.get(shipId);
        }

        return null;
    }

    public void setAssignment(int shipId, Assignment assignment) {
        assignments.put(shipId, assignment);
    }

    public void updateLastSeen(int shipId) {
        lastSeen.put(shipId, turnNumber);
    }

    public Map<Integer, Path> getShipPaths() {
        return shipPath;
    }

    public void addMove(Move move) {
        moveList.add(move);

        // store any thrust moves to avoid collisions

        if (move.getType() == Move.MoveType.Thrust) {
            final ThrustMove thrustMove = (ThrustMove)move;
            final Ship ship = move.getShip();

            final double velocity = thrustMove.getThrust();
            final double degrees = thrustMove.getAngle();
            final double theta = Math.toRadians(degrees);

            final double dx = Math.cos(theta) * velocity;
            final double dy = Math.sin(theta) * velocity;

            final Position start = new Position(ship.getXPos(), ship.getYPos());
            final Position end = new Position(start.getXPos() + dx, start.getYPos() + dy);
            final Path p = new Path(start, end);

            Log.log("Ship " + ship.getId() + " start = " + start.toString() + ", end = " + end.toString());

            shipPath.put(ship.getId(), p);
        }
    }
}
