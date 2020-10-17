import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;

class PoolBall extends Circle {

    static double BALL_RADIUS = 0.0285 * PoolTable.PIXELS_PER_METER;
    static double NUMBERED_BALL_MASS = 0.16;
    static double CUE_BALL_MASS = 0.17;
    static double MAX_PADDING = PoolTable.TABLE_PADDING + PoolTable.BUMPER_WIDTH + BALL_RADIUS;

    // Milliseconds since UNIX epoch
    private long timeOfLastCollision;
    private long timeOfNextCollision;

    private Orientation orientationOfNextCollision;

    private BallNumber identity;

    // Various properties of the ball
    private boolean obstructed;
    private boolean moving;
    private boolean sunk;
    private boolean sinkNextCollision;

    // Measured in meters, with center of table as the origin
    private double xLastCollision;
    private double yLastCollision;
    private double xNextCollision;
    private double yNextCollision;

    // Measured in m/s
    private double speedLastCollision;
    private double speedNextCollision;
    private double speed;

    // Measured in radians, counterclockwise from positive x-axis, between -pi/2 and pi/2
    private double direction;

    // For testing only
//    private Polyline ballPath;

    private Timeline animation;

    PoolBall(double centerX, double centerY, BallNumber identity) {
        super(centerX, centerY, BALL_RADIUS);
        timeOfLastCollision = java.lang.System.currentTimeMillis();
        this.identity = identity;
        xLastCollision = centerX / PoolTable.PIXELS_PER_METER;
        yLastCollision = centerY / PoolTable.PIXELS_PER_METER;
        moving = false;
        sunk = false;
        obstructed = false;
        sinkNextCollision = false;
        speedLastCollision = 0;
        direction = 0;

        setParamsNextCollision();

        // For testing only
//        ballPath = new Polyline(centerX, centerY);

        animation = new Timeline(new KeyFrame(Duration.millis(25), e-> {
            if (!sunk) {
                update();
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

    }

    boolean isMoving() {
        return moving;
    }

    boolean isSunk() {
        return sunk;
    }

    boolean isObstructed() {
        return obstructed;
    }

    BallNumber getIdentity() {
        return identity;
    }

    double getSpeed() {
        return speed;
    }

    double getDirection() {
        return direction;
    }

    // FOR TESTING PURPOSES ONLY
//    void setSpeed(double speed) {
//        this.speed = speed;
//        speedLastCollision = speed;
//        moving = speed != 0;
//        direction = Math.random() * Math.PI * 2 - Math.PI;
//        setParamsNextCollision();
//    }

    // For testing only
//    Polyline getBallPath() {
//        return ballPath;
//    }

    void setSunk(boolean sunk) {
        this.sunk = sunk;
    }

    void setObstructed(boolean obstructed) {
        this.obstructed = obstructed;
    }

    void shoot(double speed, double direction) {
        if (identity == BallNumber.CUE_BALL) {
            sinkNextCollision = false;
            sunk = false;
            xLastCollision = super.getCenterX() / PoolTable.PIXELS_PER_METER;
            yLastCollision = super.getCenterY() / PoolTable.PIXELS_PER_METER;
            timeOfLastCollision = java.lang.System.currentTimeMillis();
            speedLastCollision = speed;
            this.speed = speed;
            this.direction = direction;
            setParamsNextCollision();
            moving = true;
        }
    }

    // Updates centerX, centerY, and speed using timeElapsedSecs, xLastCollision, yLastCollision, speedLastCollision,
    // and direction. Uses a coefficient of rolling resistance of 0.01 and acceleration due to gravity of 9.8 m/s^2
    private void update() {
        if (moving && !obstructed) {
            boolean collisionOccurred = timeOfNextCollision > 0 &&
                    java.lang.System.currentTimeMillis() > timeOfNextCollision;
            if (collisionOccurred) {
                if (sinkNextCollision) {
                    sunk = true;
                }
                else {
                    timeOfLastCollision = timeOfNextCollision;
                    speedLastCollision = speedNextCollision;
                    xLastCollision = xNextCollision;
                    yLastCollision = yNextCollision;
                    if (orientationOfNextCollision == Orientation.HORIZONTAL) {
                        direction *= -1;
                    } else {
                        if (direction > 0) {
                            direction = Math.PI - direction;
                        } else {
                            direction = Math.PI * -1 - direction;
                        }
                    }
                }
            }
            double timeElapsedSecs = (java.lang.System.currentTimeMillis() - timeOfLastCollision) / 1000.0;
            speed = Math.max(speedLastCollision - 0.098 * timeElapsedSecs, 0);
            moving = speed != 0;
            if (moving) {
                double velocityX = Math.cos(direction) * speedLastCollision;
                double velocityY = Math.sin(direction) * speedLastCollision;
                double accelerationX = Math.cos(direction) * -0.098;
                double accelerationY = Math.sin(direction) * -0.098;
                double newX = xLastCollision + velocityX * timeElapsedSecs +
                        0.5 * accelerationX * Math.pow(timeElapsedSecs, 2);
                double newY = yLastCollision + velocityY * timeElapsedSecs +
                        0.5 * accelerationY * Math.pow(timeElapsedSecs, 2);
                super.setCenterX(newX * PoolTable.PIXELS_PER_METER);
                super.setCenterY(newY * PoolTable.PIXELS_PER_METER);

                // For testing only
                // ballPath.getPoints().addAll(super.getCenterX(), super.getCenterY());

                if (collisionOccurred) {
                    setParamsNextCollision();
                }
            }
        }
    }

    private void setParamsNextCollision() {

        double velocityX = Math.cos(direction) * speedLastCollision;
        double velocityY = Math.sin(direction) * speedLastCollision;
        double accelerationX = Math.cos(direction) * -0.098;
        double accelerationY = Math.sin(direction) * -0.098;

        // Calculate time until collision with top or bottom bumper
        long timeElapsedY;
        double velocityFinalYSquared;
        if (direction < 0) {
            velocityFinalYSquared = Math.pow(velocityY, 2) + 2 * accelerationY * (MAX_PADDING / PoolTable.PIXELS_PER_METER - yLastCollision);
            if (velocityFinalYSquared > 0) {
                timeElapsedY = (long)(((Math.sqrt(velocityFinalYSquared) * -1 - velocityY) / accelerationY) * 1000);
            }
            else {
                timeElapsedY = -1;
            }
        }
        else {
            velocityFinalYSquared = Math.pow(velocityY, 2) + 2 * accelerationY * ((PoolTable.SCENE_HEIGHT - MAX_PADDING) / PoolTable.PIXELS_PER_METER - yLastCollision);
            if (velocityFinalYSquared > 0) {
                timeElapsedY = (long)(((Math.sqrt(velocityFinalYSquared) - velocityY) / accelerationY) * 1000);
            }
            else {
                timeElapsedY = -1;
            }
        }

        // Calculate time until collision with left or right bumper
        long timeElapsedX;
        double velocityFinalXSquared;
        if (Math.abs(direction) > Math.PI / 2) {
            velocityFinalXSquared = Math.pow(velocityX, 2) + 2 * accelerationX * (MAX_PADDING / PoolTable.PIXELS_PER_METER - xLastCollision);
            if (velocityFinalXSquared > 0) {
                timeElapsedX = (long)(((Math.sqrt(velocityFinalXSquared) * -1 - velocityX) / accelerationX) * 1000);
            }
            else {
                timeElapsedX = -1;
            }
        }
        else {
            velocityFinalXSquared = Math.pow(velocityX, 2) + 2 * accelerationX * ((PoolTable.SCENE_WIDTH - MAX_PADDING) / PoolTable.PIXELS_PER_METER - xLastCollision);
            if (velocityFinalXSquared > 0) {
                timeElapsedX = (long)(((Math.sqrt(velocityFinalXSquared) - velocityX) / accelerationX) * 1000);
            }
            else {
                timeElapsedX = -1;
            }
        }


        // Assign shortest positive collision time to timeOfNextCollision, or if none exists, a negative number
        if (timeElapsedX < 0 && timeElapsedY < 0) {
            timeOfNextCollision = -1;
        }
        else if (timeElapsedX < 0) {
            timeOfNextCollision = timeElapsedY + timeOfLastCollision;
            orientationOfNextCollision = Orientation.HORIZONTAL;
        }
        else if (timeElapsedY < 0) {
            timeOfNextCollision = timeElapsedX + timeOfLastCollision;
            orientationOfNextCollision = Orientation.VERTICAL;
        }
        else {
            timeOfNextCollision = Math.min(timeElapsedX, timeElapsedY) + timeOfLastCollision;
            orientationOfNextCollision = (timeElapsedX > timeElapsedY ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        }

        double timeElapsedSecs = (timeOfNextCollision - timeOfLastCollision) / 1000.0;
        speedNextCollision = Math.max(speedLastCollision - 0.098 * timeElapsedSecs, 0);
        xNextCollision = xLastCollision + velocityX * timeElapsedSecs +
                0.5 * accelerationX * Math.pow(timeElapsedSecs, 2);
        yNextCollision = yLastCollision + velocityY * timeElapsedSecs +
                0.5 * accelerationY * Math.pow(timeElapsedSecs, 2);
        if ((xNextCollision * PoolTable.PIXELS_PER_METER >
                PoolTable.SCENE_WIDTH / 2 - PoolTable.POCKET_RADIUS + PoolTable.BUMPER_WIDTH &&
                xNextCollision * PoolTable.PIXELS_PER_METER <
                        PoolTable.SCENE_WIDTH / 2 + PoolTable.POCKET_RADIUS - PoolTable.BUMPER_WIDTH) ||
                (xNextCollision * PoolTable.PIXELS_PER_METER <
                        PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS &&
                        (yNextCollision * PoolTable.PIXELS_PER_METER <
                                PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS ||
                                yNextCollision * PoolTable.PIXELS_PER_METER >
                                        PoolTable.SCENE_HEIGHT - (PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS))) ||
                (xNextCollision * PoolTable.PIXELS_PER_METER >
                        PoolTable.SCENE_WIDTH - (PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS) &&
                        (yNextCollision * PoolTable.PIXELS_PER_METER <
                                PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS ||
                                yNextCollision * PoolTable.PIXELS_PER_METER >
                                        PoolTable.SCENE_HEIGHT - (PoolTable.TABLE_PADDING + PoolTable.POCKET_RADIUS)))
        ) {
            sinkNextCollision = true;
        }
    }

    static void collide(PoolBall b1, PoolBall b2) {
        b1.collide(b2);
        b2.collide(b1);
    }

    private void collide(PoolBall otherBall) {
        xLastCollision = super.getCenterX() / PoolTable.PIXELS_PER_METER;
        yLastCollision = super.getCenterY() / PoolTable.PIXELS_PER_METER;
        timeOfLastCollision = java.lang.System.currentTimeMillis();
        // TODO: Update speed, speedLastCollision, and direction based on speed and direction of otherBall
        setParamsNextCollision();
    }

}

enum BallNumber {
    CUE_BALL,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13,B14,B15
}
