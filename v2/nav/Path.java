package v2.nav;

import hlt.Position;

public class Path {
    private Position start, end;

    public Path(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Path{" +
                "(" + start.getXPos() + ", " + start.getYPos() + ") > " +
                "(" + end.getXPos() + ", " + end.getYPos() + ")" +
                '}';
    }
}
