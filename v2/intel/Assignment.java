package v2.intel;

import hlt.GameMap;
import hlt.Ship;

public class Assignment {
    private Task goal;
    private int strategyId; // reference to the strategy that created this assignment
    private int shipId; // reference to the ship with this assignment

    public Task getGoal() {
        return goal;
    }

    public void setGoal(Task goal) {
        this.goal = goal;
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public int getStrategyId() {

        return strategyId;
    }

    public void setStrategyId(int strategyId) {
        this.strategyId = strategyId;
    }

    public boolean canBeCompleted(Ship ship, GameMap gameMap) {
        return goal.canBeCompleted(ship, gameMap);
    }

    public boolean isComplete(Ship ship, GameMap gameMap) {
        return goal.isComplete(ship, gameMap);
    }

    @Override
    public String toString() {
        return "Assignment{" +
            "goal=" + goal +
            ", shipId=" + shipId +
            ", strategyId=" + strategyId +
            '}';
    }

    public Assignment() {}
}
