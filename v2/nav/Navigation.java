package v2.nav;

import hlt.*;

import java.util.Map;
import java.util.TreeMap;

public class Navigation {
    private final double WILL_CRASH = 7d;
    private final double LOOK_AHEAD_DISTANCE = 14d;
    private final double FUDGE = Constants.SHIP_RADIUS;

    public Navigation() {}

    public Move navigateShipTowardsTarget(GameMap gameMap, Ship ship, Position target, int speed, Map<Integer, Path> shipPaths) {
        return navigateShipToTargetV4(gameMap, ship, target, speed, shipPaths);
    }

    // todo: get rid of this? or leave it since it should be a clear path by the time this is called OR set avoidObstacles to false
    private Move hltNavigateShipTowardsTarget(GameMap gameMap, Ship ship, Position target, int speed, boolean avoidObstacles) {
        return hlt.Navigation.navigateShipTowardsTarget(gameMap, ship, target, speed, avoidObstacles, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
    }

    public Move navigateShipToDock(GameMap gameMap, Ship ship, Planet planet, Map<Integer, Path> shipPaths) {
        final Planet realPlanet = gameMap.getPlanet(planet.getId());

        if (ship.canDock(realPlanet)) {
            Log.log("ship " + ship.getId() + " can dock on planet " + planet.getId());
            return new DockMove(ship, realPlanet);
        } else {
            final Position targetPosition = ship.getClosestPoint(realPlanet);
            return navigateShipTowardsTarget(gameMap, ship, targetPosition, Constants.MAX_SPEED, shipPaths);
        }
    }

    private Move navigateShipToTargetV4(GameMap gameMap, Ship ship, Position target, int speed, Map<Integer, Path> shipPaths) {
        if (speed < 1) {
            // if the ship cannot move, do not create a move
            return null;
        }

        if (shipPaths != null) {
            final double theta = ship.orientTowardsInRad(target);
            final double newX = Math.cos(theta) * (double)speed;
            final double newY = Math.sin(theta) * (double)speed;
            final Position newPosition = new Position(ship.getXPos() + newX, ship.getYPos() + newY);
            final Path pTest = new Path(ship, newPosition);

            // for each path, see if this path will collide with it
            for (int shipId : shipPaths.keySet()) {
                final Path path = shipPaths.get(shipId);
                final Position intersection = findIntersection(pTest, path);
                final double distanceBetweenPaths = findDistanceBetweenPaths(pTest, path);

                Log.log("Ship " + ship.getId() + " " + pTest);
                Log.log("Ship " + shipId + " " + path);

                if (intersection == null && distanceBetweenPaths > 1d) {
                    Log.log("Paths are parallel and > 1d apart");
                    continue;
                }

                if (intersection == null) {
                    Log.log("Paths are parallel, but too close");
                    return navigateShipToTargetV4(gameMap, ship, newPosition, speed-1, shipPaths);
                }

                // check the path for a collision
                final double distanceToCollide = ship.getDistanceTo(intersection);
                Log.log("Distance between ship " + ship.getId() + " and ship " + shipId + " = " + distanceToCollide + ", v = " + speed);

                // todo: do something better here
                // todo: if this ship is heading the same direction as the other ship, keep going,
                // todo: if they're going to crash, pick a new target parallel to the existing path

                //todo: get intersection point, if it's > speed away, continue

                if (distanceToCollide - (double)speed > 1d) {
                    Log.log("distance - velocity > 1d");
                    continue;
                }

                // try reducing speed
                final int newSpeed = speed-1;
                final double safeX = Math.cos(theta) * (double)newSpeed;
                final double safeY = Math.sin(theta) * (double)newSpeed;
                final Position safePosition = new Position(ship.getXPos() + safeX, ship.getYPos() + safeY);

                return navigateShipToTargetV4(gameMap, ship, safePosition, newSpeed, shipPaths);
            }
        }

        return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
    }

    private Move navigateShipToTargetV3(GameMap gameMap, Ship ship, Position target, int speed, Map<Integer, Path> shipPaths) {
        if (speed < 1) {
            // if the ship cannot move, do not create a move
            return null;
        }

        if (shipPaths != null) {
            final double theta = ship.orientTowardsInRad(target);
            final double newX = Math.cos(theta) * (double)speed;
            final double newY = Math.sin(theta) * (double)speed;
            final Position newPosition = new Position(ship.getXPos() + newX, ship.getYPos() + newY);
            final Path pTest = new Path(ship, newPosition);

            // for each path, see if this path will collide with it
            for (int shipId : shipPaths.keySet()) {
                final Path path = shipPaths.get(shipId);
                final boolean pathsCross = pathsIntersect(pTest, path);

                Log.log("Ship " + ship.getId() + " " + pTest);
                Log.log("Ship " + shipId + " " + path);

                if (!pathsCross) {
                    Log.log("Paths are parallel");
                    continue;
                }

                // check the path for a collision
                final double distanceBetweenPaths = findDistanceBetweenPaths(pTest, path);
                Log.log("Distance between ship " + ship.getId() + " and ship " + shipId + " = " + distanceBetweenPaths);

                // todo: do something better here
                // todo: if this ship is heading the same direction as the other ship, keep going,
                // todo: if they're going to crash, pick a new target parallel to the existing path

                //todo: get intersection point, if it's > speed away, continue

                if (distanceBetweenPaths < speed) {
                    // try reducing speed
                    final int thrust = (int) (distanceBetweenPaths - Constants.SHIP_RADIUS);
                    return navigateShipToTargetV3(gameMap, ship, newPosition, thrust, shipPaths);
                }
            }
        }

        return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
    }

    private Move navigateShipToTargetV2(GameMap gameMap, Ship ship, Position target, int speed, Map<Integer, Path> shipPaths) {
//        if (shipPaths != null) {
//            for (Path path : shipPaths.values()) {
//                if (ship.getDistanceTo(path.getStart()) <= 1d || ship.getDistanceTo(path.getEnd()) <= 1d) {
//                    return null;
//                }
//            }
//        }

        if (shipPaths != null) {
            for (Path path : shipPaths.values()) {
                // check the path for a collision
                final Path pTest = new Path(ship, target);
                final double distanceBetweenPaths = findDistanceBetweenPaths(pTest, path);

                // todo: do something better here
                // todo: if this ship is heading the same direction as the other ship, keep going,
                // todo: if they're going to crash, pick a new target parallel to the existing path

                if (distanceBetweenPaths <= 1d) {
                    // todo: figure out if i should be doing something different here
                    return hltNavigateShipTowardsTarget(gameMap, ship, target, 1, true);
                }
            }
        }

        return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
    }

    private Move navigateShipToTargetV1(GameMap gameMap, Ship ship, Position target, int speed, Map<Integer, Path> shipPaths) {
        final Map<Double, Entity> nearbyEntities = gameMap.nearbyEntitiesByDistance(ship);
        if (nearbyEntities == null) {
            return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
        }

        for (final double distance : nearbyEntities.keySet()) {
            // nothing in the way, keep going
            if (distance > LOOK_AHEAD_DISTANCE) {
                return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
            }

            final Entity entity = nearbyEntities.get(distance);

            // will we collide with planet?
            if (entity instanceof Planet) {
                final Map<Double, Position> collisionPoints = getCollisionPoints(ship, target, entity);
                if (collisionPoints != null) {
                    for (final double distanceToCollide : collisionPoints.keySet()) {
                        // nothing in the way, keep going
                        if (distanceToCollide > LOOK_AHEAD_DISTANCE) {
                            return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
                        }

                        final Position closestPointOnPath = closestPointOnLineSegment(ship, target, entity);
                        final Position safePosition = pointOnEdge(closestPointOnPath, entity);

                        return hltNavigateShipTowardsTarget(gameMap, ship, safePosition, speed, true);
                    }
                } else {
                    return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
                }
            } else if (entity instanceof Ship) {
                if (shipPaths != null) {
                    final Path shipPath = shipPaths.get(entity.getId());
                    if (shipPath != null) {
                        // check the path for a collision
                        final Path pTest = new Path(ship, target);
                        final double distanceBetweenPaths = findDistanceBetweenPaths(pTest, shipPath);
                        if (distanceBetweenPaths <= 1d) {
                            // todo: figure out if i should be doing something different here
                            return hltNavigateShipTowardsTarget(gameMap, ship, target, 1, true);
                        } else {
                            return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
                        }
                    }
                }

                final Map<Double, Position> collisionPoints = getCollisionPoints(ship, target, entity);
                if (collisionPoints != null) {
                    for (final double distanceToCollide : collisionPoints.keySet()) {
                        // nothing in the way, keep going
                        if (distanceToCollide > WILL_CRASH) {
                            return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
                        }

                        final Position closestPointOnPath = closestPointOnLineSegment(ship, target, entity);
                        final Position safePosition = pointOnEdge(closestPointOnPath, entity);

                        return hltNavigateShipTowardsTarget(gameMap, ship, safePosition, speed, true);
                    }
                } else {
                    return hltNavigateShipTowardsTarget(gameMap, ship, target, speed, true);
                }
            }
        }

        return null;
    }

    private Position findIntersection(Path p1, Path p2) {
        // Get the segments' parameters.
        final double dx12 = p2.getStart().getXPos() - p1.getStart().getXPos();
        final double dy12 = p2.getStart().getYPos() - p1.getStart().getYPos();
        final double dx34 = p2.getEnd().getXPos() - p1.getEnd().getXPos();
        final double dy34 = p2.getEnd().getYPos() - p1.getEnd().getYPos();

        // Solve for t1 and t2
        final double denominator = (dy12 * dx34 - dx12 * dy34);

        if (denominator == 0d) {
            // cannot divide by 0, lines are parallel
            return null;
        }

        final double t1 = ((p1.getStart().getXPos() - p2.getStart().getXPos()) * dy34 + (p2.getStart().getYPos() - p1.getStart().getYPos()) * dx34) / denominator;

        if (Double.isInfinite(t1)) {
            // The lines are parallel (or close enough to it).
            return null;
        }

        // Find the point of intersection.
        return new Position(p1.getStart().getXPos() + dx12 * t1, p1.getStart().getYPos() + dy12 * t1);
    }

    private boolean pathsIntersect(Path p1, Path p2) {
        // Get the segments' parameters.
        final double dx12 = p2.getStart().getXPos() - p1.getStart().getXPos();
        final double dy12 = p2.getStart().getYPos() - p1.getStart().getYPos();
        final double dx34 = p2.getEnd().getXPos() - p1.getEnd().getXPos();
        final double dy34 = p2.getEnd().getYPos() - p1.getEnd().getYPos();

        // Solve for t1 and t2
        final double denominator = (dy12 * dx34 - dx12 * dy34);

        if (denominator == 0d) {
            // cannot divide by 0, lines are parallel
            return false;
        }

        final double t1 = ((p1.getStart().getXPos() - p2.getStart().getXPos()) * dy34 + (p2.getStart().getYPos() - p1.getStart().getYPos()) * dx34) / denominator;

        if (Double.isInfinite(t1)) {
            // The lines are parallel (or close enough to it).
            return false;
        }

        return true;
    }

    private double findDistanceToSegment(Position position, Path path) {
        double dx = path.getEnd().getXPos() - path.getStart().getXPos();
        double dy = path.getEnd().getYPos() - path.getStart().getYPos();

        if ((dx == 0) && (dy == 0)) {
            // It's a point not a line segment.
            return position.getDistanceTo(path.getEnd());
        }

        // Calculate the t that minimizes the distance.
        double t = ((position.getXPos() - path.getStart().getXPos()) * dx + (position.getYPos() - path.getStart().getYPos()) * dy) /
                (dx * dx + dy * dy);

        // See if this represents one of the segment's
        // end points or a point in the middle.
        if (t < 0) {
            return position.getDistanceTo(path.getStart());
        } else if (t > 1) {
            return position.getDistanceTo(path.getEnd());
        } else {
            final Position closest = new Position(path.getStart().getXPos() + t * dx, path.getStart().getYPos() + t * dy);
            return position.getDistanceTo(closest);
        }
    }

    // find the distance between the closest point on 2 paths
    private double findDistanceBetweenPaths(Path p1, Path p2) {
        double distance;
        double temp;

        // test p1.start
        distance = findDistanceToSegment(p1.getStart(), p2);

        // test p1.end
        temp = findDistanceToSegment(p1.getEnd(), p2);
        if (temp < distance) {
            distance = temp;
        }

        // test p2.start
        temp = findDistanceToSegment(p2.getStart(), p1);
        if (temp < distance) {
            distance = temp;
        }

        // test p2.end
        temp = findDistanceToSegment(p2.getEnd(), p1);
        if (temp < distance) {
            distance = temp;
        }

        return distance;
    }

    // get all points where the line between start and end intersects with entity
    private Map<Double, Position> getCollisionPoints(Position start, Position end, Entity entity) {
        final Map<Double, Position> collisionPoints = new TreeMap<>();

        final double baX = end.getXPos() - start.getXPos();
        final double baY = end.getYPos() - start.getYPos();
        final double caX = entity.getXPos() - start.getXPos();
        final double caY = entity.getYPos() - start.getYPos();
        final double r = entity.getRadius()+ FUDGE + Constants.SHIP_RADIUS;

        final double a = baX * baX + baY * baY;
        final double bBy2 = baX * caX + baY * caY;
        final double c = caX * caX + caY * caY - r * r;

        final double pBy2 = bBy2 / a;
        final double q = c / a;

        final double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return null;
        }

        // if disc == 0 ... dealt with later
        final double tmpSqrt = Math.sqrt(disc);
        final double abScalingFactor1 = -pBy2 + tmpSqrt;
        final double abScalingFactor2 = -pBy2 - tmpSqrt;

        final Position p1 = new Position(start.getXPos() - baX * abScalingFactor1, start.getYPos() - baY * abScalingFactor1);
        collisionPoints.put(start.getDistanceTo(p1), p1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return collisionPoints;
        }

        final Position p2 = new Position(start.getXPos() - baX * abScalingFactor2, start.getYPos() - baY * abScalingFactor2);
        collisionPoints.put(start.getDistanceTo(p2), p2);
        return collisionPoints;
    }

    // get the closest point on a line segment to another point
    private Position closestPointOnLineSegment(Position start, Position end, Position entity) {
        final double dx = end.getXPos() - start.getXPos();
        final double dy = end.getYPos() - start.getYPos();
        final double d2 = dx * dx + dy * dy;
        double nx = ((entity.getXPos()-start.getXPos()) * dx + (entity.getYPos() - start.getYPos()) * dy) / d2;
        if (nx < 0) {
            nx = 0;
        } else if (nx > 1) {
            nx = 1;
        }

        return new Position(dx*nx + start.getXPos(), dy*nx + start.getYPos());
    }

    // get the point on the edge of entity that we can navigate to without colliding with entity
    private Position pointOnEdge(Position angleTowards, Entity entity) {
        final double radians = entity.orientTowardsInRad(angleTowards);
        final double r = entity.getRadius() + FUDGE + Constants.SHIP_RADIUS;
        final double cX = entity.getXPos() + (r * Math.cos(radians));
        final double cY = entity.getYPos() + (r * Math.sin(radians));

        return new Position(cX, cY);
    }
}
