package v2.intel;

import hlt.*;

public abstract class Task {
    Move.MoveType moveType;
    Position targetPosition;
    Planet targetPlanet;
    Ship targetShip;

    public Task() {}

    public Move.MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(Move.MoveType moveType) {
        this.moveType = moveType;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Position targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Planet getTargetPlanet() {
        return targetPlanet;
    }

    public void setTargetPlanet(Planet targetPlanet) {
        this.targetPlanet = targetPlanet;
    }

    public Ship getTargetShip() {
        return targetShip;
    }

    public void setTargetShip(Ship targetShip) {
        this.targetShip = targetShip;
    }

    public abstract boolean canBeCompleted(Ship ship, GameMap gameMap);

    public abstract boolean isComplete(Ship ship, GameMap gameMap);

    @Override
    public String toString() {
        return "Task{" +
            "moveType=" + moveType +
            ", targetPosition=" + targetPosition +
            ", targetPlanet=" + targetPlanet +
            ", targetShip=" + targetShip +
            '}';
    }
}
