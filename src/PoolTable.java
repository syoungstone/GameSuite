import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class PoolTable<vBox> extends Pane {

    static double PIXELS_PER_METER = 200;

    private static final double TABLE_WIDTH = 2.62 * PIXELS_PER_METER;
    private static final double TABLE_HEIGHT = 1.5 * PIXELS_PER_METER;
    private static final double PLAYABLE_WIDTH = 2.24 * PIXELS_PER_METER;
    private static final double PLAYABLE_HEIGHT = 1.12 * PIXELS_PER_METER;
    static double BUMPER_WIDTH = 0.03 * PIXELS_PER_METER;
    private static final double CUE_LENGTH = 1.4 * PIXELS_PER_METER;
    static double POCKET_RADIUS = 0.065 * PIXELS_PER_METER;

    private static final double FELT_WIDTH = PoolTable.PLAYABLE_WIDTH + PoolTable.BUMPER_WIDTH * 2;
    private static final double FELT_HEIGHT = PoolTable.PLAYABLE_HEIGHT + PoolTable.BUMPER_WIDTH * 2;
    static double TABLE_PADDING = CUE_LENGTH / 2 + (TABLE_WIDTH - FELT_WIDTH) / 2;

    static double SCENE_WIDTH = TABLE_WIDTH + CUE_LENGTH;
    static double SCENE_HEIGHT = TABLE_HEIGHT + CUE_LENGTH;

    private static final Circle HOLE_1 = new Circle(TABLE_PADDING, TABLE_PADDING, POCKET_RADIUS, Color.BLACK);
    private static final Circle HOLE_2 = new Circle(SCENE_WIDTH - TABLE_PADDING, TABLE_PADDING, POCKET_RADIUS, Color.BLACK);
    private static final Circle HOLE_3 = new Circle(TABLE_PADDING, SCENE_HEIGHT - TABLE_PADDING, POCKET_RADIUS, Color.BLACK);
    private static final Circle HOLE_4 = new Circle(SCENE_WIDTH - TABLE_PADDING,
            SCENE_HEIGHT - TABLE_PADDING, POCKET_RADIUS, Color.BLACK);
    private static final Circle HOLE_5 = new Circle(SCENE_WIDTH / 2,
            TABLE_PADDING + (BUMPER_WIDTH - POCKET_RADIUS), POCKET_RADIUS, Color.BLACK);
    private static final Circle HOLE_6 = new Circle(SCENE_WIDTH / 2,
            SCENE_HEIGHT - TABLE_PADDING - (BUMPER_WIDTH - POCKET_RADIUS), POCKET_RADIUS, Color.BLACK);
    private static final Circle[] HOLES = new Circle[]{HOLE_1, HOLE_2, HOLE_3, HOLE_4, HOLE_5, HOLE_6};

    private final Rectangle felt = new Rectangle(TABLE_PADDING, TABLE_PADDING, FELT_WIDTH, FELT_HEIGHT);
    private final Rectangle table = new Rectangle(CUE_LENGTH / 2, CUE_LENGTH / 2, TABLE_WIDTH, TABLE_HEIGHT);

    private boolean inMotion = true;

    private boolean cueBallLocationPicked;

    private final Polygon cue = new Polygon();
    private final Polygon cueOverlay = new Polygon();
    private final Text message = new Text("Welcome to 8-ball Pool");
    private final Text hint = new Text("Try to sink the cue ball");
    private double angle;
    private boolean angleChosen;
    private boolean shotStrengthChosen;

    private final Slider powerSlider = new Slider(0, 5, 0);
    private final Button launchButton = new Button("Take shot");
    private final VBox shotDecider = new VBox(powerSlider, launchButton);
    private final BorderPane controlPane = new BorderPane(null, message, null, hint, null);

    // Holds all balls involved in the game
    private final ArrayList<PoolBall> allBalls;

    // Holds all balls currently unsunk
    private final ArrayList<PoolBall> unsunkBalls;

    private final Timeline animation;

    PoolTable() {
        super();

        shotDecider.setAlignment(Pos.CENTER);
        shotDecider.setSpacing(10);
        shotDecider.setPadding(new Insets(0,30,0,0));

        controlPane.setPrefWidth(SCENE_WIDTH);
        controlPane.setPrefHeight(SCENE_HEIGHT);
        controlPane.setPadding(new Insets(40, 0, 40, 0));

        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrappingWidth(SCENE_WIDTH);
        message.setFont(new Font(35));
        message.setTextOrigin(VPos.TOP);

        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setWrappingWidth(SCENE_WIDTH);
        hint.setFont(new Font(35));
        hint.setTextOrigin(VPos.TOP);

        felt.setArcHeight(25);
        felt.setArcWidth(25);
        table.setArcHeight(30);
        table.setArcWidth(30);

        felt.setFill(new Color(39 / 255.0, 178 / 255.0, 115 / 255.0, 1));
        table.setFill(Color.SADDLEBROWN);

        felt.setStrokeWidth(0);
        table.setStrokeWidth(0);

        super.getChildren().addAll(table, felt, controlPane);

        super.getChildren().addAll(HOLES);

        Polygon topBumper1 = new Polygon(
                TABLE_PADDING + BUMPER_WIDTH * 2, TABLE_PADDING,
                TABLE_PADDING + BUMPER_WIDTH * 3, TABLE_PADDING + BUMPER_WIDTH,
                SCENE_WIDTH / 2 - POCKET_RADIUS, TABLE_PADDING + BUMPER_WIDTH,
                SCENE_WIDTH / 2 - POCKET_RADIUS + BUMPER_WIDTH, TABLE_PADDING
        );
        Polygon topBumper2 = new Polygon(
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH * 2), TABLE_PADDING,
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH * 3), TABLE_PADDING + BUMPER_WIDTH,
                SCENE_WIDTH / 2 + POCKET_RADIUS, TABLE_PADDING + BUMPER_WIDTH,
                SCENE_WIDTH / 2 + POCKET_RADIUS - BUMPER_WIDTH, TABLE_PADDING
        );
        Polygon bottomBumper1 = new Polygon(
                TABLE_PADDING + BUMPER_WIDTH * 2, SCENE_HEIGHT - TABLE_PADDING,
                TABLE_PADDING + BUMPER_WIDTH * 3, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH),
                SCENE_WIDTH / 2 - POCKET_RADIUS, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH),
                SCENE_WIDTH / 2 - POCKET_RADIUS + BUMPER_WIDTH, SCENE_HEIGHT - TABLE_PADDING
        );
        Polygon bottomBumper2 = new Polygon(
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH * 2), SCENE_HEIGHT - TABLE_PADDING,
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH * 3), SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH),
                SCENE_WIDTH / 2 + POCKET_RADIUS, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH),
                SCENE_WIDTH / 2 + POCKET_RADIUS - BUMPER_WIDTH, SCENE_HEIGHT - TABLE_PADDING
        );
        Polygon leftBumper = new Polygon(
                TABLE_PADDING, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH * 2),
                TABLE_PADDING, TABLE_PADDING + BUMPER_WIDTH * 2,
                TABLE_PADDING + BUMPER_WIDTH, TABLE_PADDING + BUMPER_WIDTH * 3,
                TABLE_PADDING + BUMPER_WIDTH, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH * 3)
        );
        Polygon rightBumper = new Polygon(
                SCENE_WIDTH - TABLE_PADDING, SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH * 2),
                SCENE_WIDTH - TABLE_PADDING, TABLE_PADDING + BUMPER_WIDTH * 2,
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH), TABLE_PADDING + BUMPER_WIDTH * 3,
                SCENE_WIDTH - (TABLE_PADDING + BUMPER_WIDTH), SCENE_HEIGHT - (TABLE_PADDING + BUMPER_WIDTH * 3)
        );
        Polygon[] bumpers = new Polygon[] {
                topBumper1, topBumper2, bottomBumper1, bottomBumper2, leftBumper, rightBumper
        };
        for (Polygon bumper : bumpers) {
            bumper.setFill(new Color(36 / 255.0, 136 / 255.0, 78 / 255.0, 1));
        }
        super.getChildren().addAll(bumpers);

        animation = new Timeline(new KeyFrame(Duration.millis(25), e-> {
            if (inMotion) {
                update();
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

//        BallNumber[] ballNumbers = {BallNumber.CUE_BALL, BallNumber.B1, BallNumber.B2,
//                BallNumber.B3, BallNumber.B4, BallNumber.B5, BallNumber.B6,
//                BallNumber.B7, BallNumber.B8, BallNumber.B9, BallNumber.B10,
//                BallNumber.B11, BallNumber.B12, BallNumber.B13, BallNumber.B14,
//                BallNumber.B15};

        BallNumber[] ballNumbers = {BallNumber.CUE_BALL};

        allBalls = new ArrayList<>();
        unsunkBalls = new ArrayList<>();

        Color[] ballColors = {Color.WHITE,
                new Color(244 / 255.0, 150 / 255.0, 4 / 255.0, 1),
                new Color(1 / 255.0, 1 / 255.0, 128 / 255.0, 1),
                new Color(216 / 255.0, 0 / 255.0, 0, 1),
                new Color(77 / 255.0, 1 / 255.0, 117 / 255.0, 1),
                new Color(246 / 255.0, 93 / 255.0, 4 / 255.0, 1),
                new Color(4 / 255.0, 81 / 255.0, 4 / 255.0, 1),
                new Color(129 / 255.0, 1 / 255.0, 30 / 255.0, 1),
                Color.BLACK};

        for (BallNumber ballNumber : ballNumbers) {
            double x = Math.random() * (PLAYABLE_WIDTH - BUMPER_WIDTH * 2  - PoolBall.BALL_RADIUS) +
                    TABLE_PADDING + BUMPER_WIDTH + PoolBall.BALL_RADIUS;
            double y = Math.random() * (PLAYABLE_HEIGHT - BUMPER_WIDTH * 2 - PoolBall.BALL_RADIUS) +
                    TABLE_PADDING + BUMPER_WIDTH + PoolBall.BALL_RADIUS;
            PoolBall ball = new PoolBall(x, y, ballNumber);
            ball.setStrokeWidth(4);
//            ball.setSpeed(0.5);
            allBalls.add(ball);
            unsunkBalls.add(ball);
//            super.getChildren().add(ball.getBallPath());
        }

        for (int i = 0 ; i < allBalls.size() ; i++) {
            if (i < 9) {
                allBalls.get(i).setFill(ballColors[i]);
                allBalls.get(i).setStroke(ballColors[i]);
            }
            else {
                allBalls.get(i).setFill(Color.WHITE);
                allBalls.get(i).setStroke(ballColors[i - 8]);
            }
        }

        super.getChildren().addAll(allBalls);
    }

    private void update() {
        for (PoolBall ball : allBalls) {
            if (ball.isSunk()) {
                super.getChildren().remove(ball);
                unsunkBalls.remove(ball);
            }
        }
        boolean allStopped = true;
        for (PoolBall ball : unsunkBalls) {
            allStopped = allStopped && !ball.isMoving();
        }
        if (allStopped) {
            inMotion = false;
            if (allBalls.get(0).isSunk()) {
                cueBallLocationPicked = false;
                message.setText("Scratch! Place cue ball on table.");
                allBalls.get(0).setSunk(false);
                unsunkBalls.add(allBalls.get(0));
                super.setOnMouseMoved(e -> {
                    if (!cueBallLocationPicked) {
                        double centerX = Math.max(Math.min(e.getX(), SCENE_WIDTH - PoolBall.MAX_PADDING), PoolBall.MAX_PADDING);
                        double centerY = Math.max(Math.min(e.getY(), SCENE_HEIGHT - PoolBall.MAX_PADDING), PoolBall.MAX_PADDING);
                        allBalls.get(0).setCenterX(centerX);
                        allBalls.get(0).setCenterY(centerY);
                        if (!super.getChildren().contains(allBalls.get(0))) {
                            super.getChildren().add(allBalls.get(0));
                        }
                    }
                });
                super.setOnMouseClicked(e -> {
                    cueBallLocationPicked = true;
                    nextShot();
                });
            }
            else {
                nextShot();
            }
        }
//        for (PoolBall b1 : unsunkBalls) {
//            for (PoolBall b2 : unsunkBalls) {
//                boolean xOverlap = Math.abs(b1.getCenterX() - b2.getCenterX()) <= PoolBall.BALL_RADIUS * 2;
//                boolean yOverlap = Math.abs(b1.getCenterY() - b2.getCenterY()) <= PoolBall.BALL_RADIUS * 2;
//                if (xOverlap && yOverlap && !b1.isObstructed() && !b2.isObstructed()) {
//                    b1.setObstructed(true);
////                  b2.setObstructed(true);
//                    PoolBall.collide(b1, b2);
//                }
//            }
//        }
//        for (PoolBall ball : unsunkBalls) {
//            ball.setObstructed(false);
//        }

    }

    private void nextShot() {
        message.setText("Line up new shot:");
        angleChosen = false;
        shotStrengthChosen = false;
        cue.setFill(Color.DARKMAGENTA);
        cueOverlay.setFill(Color.BEIGE);
        super.setOnMouseMoved(e -> {
            if (!angleChosen) {
                updateCue(e.getX(), e.getY());
                if (!super.getChildren().contains(cue)) {
                    super.getChildren().addAll(cue, cueOverlay);
                }
            }
        });
        super.setOnMouseClicked(e -> {
            if (!shotStrengthChosen) {
                angleChosen = true;
                message.setText("Choose strength of shot");
                controlPane.setRight(shotDecider);
                powerSlider.setOrientation(Orientation.VERTICAL);
                launchButton.setOnAction(f -> {
                    shotStrengthChosen = true;
                    double direction = angle + (angle > 0 ? Math.PI * -1 : Math.PI);
                    allBalls.get(0).shoot(powerSlider.getValue(), direction);
                    controlPane.setRight(null);
                    message.setText("");
                    super.getChildren().removeAll(cue, cueOverlay);
                    inMotion = true;
                });
            }
        });
    }

    private void updateCue(double x, double y) {
        double cueWidthBase = 3;
        double cueWidthEnd = 6;
        double cueDistance = 15;
        double cueBallX = allBalls.get(0).getCenterX();
        double cueBallY = allBalls.get(0).getCenterY();
        double xDist = cueBallX - x;
        double yDist = cueBallY - y;
        angle = Math.atan(yDist / xDist);
        if (x < cueBallX) {
            angle = (y < cueBallY ? angle - Math.PI : angle + Math.PI);
        }
        cue.getPoints().clear();
        cue.getPoints().addAll(
                cueBallX + Math.cos(angle) * cueDistance + Math.sin(angle) * cueWidthBase,
                cueBallY + Math.sin(angle) * cueDistance - Math.cos(angle) * cueWidthBase,
                cueBallX + Math.cos(angle) * cueDistance - Math.sin(angle) * cueWidthBase,
                cueBallY + Math.sin(angle) * cueDistance + Math.cos(angle) * cueWidthBase,
                cueBallX + Math.cos(angle) * (cueDistance + CUE_LENGTH) - Math.sin(angle) * cueWidthEnd,
                cueBallY + Math.sin(angle) * (cueDistance + CUE_LENGTH) + Math.cos(angle) * cueWidthEnd,
                cueBallX + Math.cos(angle) * (cueDistance + CUE_LENGTH) + Math.sin(angle) * cueWidthEnd,
                cueBallY + Math.sin(angle) * (cueDistance + CUE_LENGTH) - Math.cos(angle) * cueWidthEnd

        );
        List<Double> cuePoints = cue.getPoints();
        cueOverlay.getPoints().clear();
        cueOverlay.getPoints().addAll(
                cuePoints.get(0), cuePoints.get(1),
                cuePoints.get(2), cuePoints.get(3),
                cuePoints.get(6), cuePoints.get(7),
                cuePoints.get(4), cuePoints.get(5)
        );
    }

    static Circle[] getHOLES() {
        return HOLES;
    }

}
