package es.ubu.lsi.perikymata.vista;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import com.sun.javafx.binding.StringFormatter;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.modelo.Measure;
import es.ubu.lsi.perikymata.util.CSVUtil;
import es.ubu.lsi.perikymata.util.ProfileUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Pair;

/**
 * Controller for the layout that is used to count perikyma.
 *
 * @author Sergio Chico Carrancio
 *
 */
public class PerikymataCountController {
	/**
	 * Measure object with the coordinates and the value of the measure.
	 */
	private Measure measure;

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * Start and end X coordinate of the deciles on the original image.
	 */
	private Double xDecileStart = null;
	private Double xDecileEnd = null;

	/**
	 * X coordinates of the deciles between the DecileStart and DecileEnd.
	 */
	private double[] decilesBetween = new double[9];

	/**
	 * Start and end Line objects drawn over the image.
	 */
	private Line lineDecileStart;
	private Line lineDecileEnd;

	/**
	 * Line objects of the deciles between DecileStart and DecileEnd
	 */
	private Line[] decilesLinesBetween = new Line[9];

	/**
	 * Free-Draw line drawn over the image.
	 */
	private Path freeDrawPath;

	/**
	 * List of points used by path to draw over the image. These List of
	 * coordinates are calculated over the original image, and are used to
	 * calculate the line when it is zoomed. The first element is always a
	 * MoveTo and the rest are LineTo.
	 */
	private List<PathElement> freeDrawPathList = FXCollections.observableArrayList();

	/**
	 * Coordinates of the detected perikymata.
	 */
	private List<int[]> peaksCoords = new ArrayList<>();

	/**
	 * Circles drawn to show where perikymata are found.
	 */
	private List<Circle> circles = FXCollections.observableArrayList();

//	/**
//	 * Drawn line from startMeasure to endMeasure.
//	 */
//	private Line measureLine;

	/**
	 * Buttons used to draw and erase perikymata.
	 */
	@FXML
	private ImageView drawPerikymataButtonImage;
	@FXML
	private ImageView erasePerikymataButtonImage;

	/**
	 * Imageview of the image used to calculate the perikymata.
	 */
	@FXML
	private ImageView croppedImageView;

//	/**
//	 * Imageview of the original image.
//	 */
//	@FXML
//	private ImageView fullOriginalImage;

	/**
	 * Label that shows the current action status.
	 */
	@FXML
	private Label statusLabel;

	/**
	 * Loading gif.
	 */
	@FXML
	private ImageView loading;

	/**
	 * Label synchronized to the slider to show its value.
	 */
	@FXML
	private Label perikymataThresholdLabel;

	/**
	 * Slider user to select the minimum threshold needed to mark a perikymata.
	 */
	@FXML
	private Slider thresholdSlider;

	/**
	 * Initializes the Javafx components.
	 */
	@FXML
	private void initialize() {
		// Original image to be shown when marking the measures.
//		fullOriginalImage.setVisible(false);
//		fullOriginalImage.fitHeightProperty().bind(croppedImageView.fitHeightProperty());
//		fullOriginalImage.fitWidthProperty().bind(croppedImageView.fitWidthProperty());
//		fullOriginalImage.eventDispatcherProperty().bind(croppedImageView.eventDispatcherProperty());

		// Loads loading gif.
		loading.setImage(new Image(this.getClass().getResource("/rsc/482.gif").toExternalForm()));
		loading.setVisible(false);

		// Show the value of the slider in the label.
		perikymataThresholdLabel.textProperty()
				.bind(((StringFormatter) Bindings.format("%.0f", thresholdSlider.valueProperty()).concat("%")));

		// Sets the parameters of the measure line.
//		measureLine = new Line();
//		measureLine.setStrokeWidth(2);
//		measureLine.setStroke(Color.RED);
//		((AnchorPane) fullImage.getParent()).getChildren().add(measureLine);

		// Sets the parameters of the decile lines.
		lineDecileStart = new Line();
		lineDecileStart.setStroke(Color.CORNFLOWERBLUE);
		((AnchorPane) croppedImageView.getParent()).getChildren().add(lineDecileStart);
		lineDecileEnd = new Line();
		lineDecileEnd.setStroke(Color.CORNFLOWERBLUE);
		((AnchorPane) croppedImageView.getParent()).getChildren().add(lineDecileEnd);

		for (int i = 0; i < decilesLinesBetween.length; i++) {
			decilesLinesBetween[i] = new Line();
			decilesLinesBetween[i].setStroke(Color.CORNFLOWERBLUE);
			((AnchorPane) croppedImageView.getParent()).getChildren().add(decilesLinesBetween[i]);
		}

		// Sets the properties for the free line used to mark perikymata.
		freeDrawPath = new Path();
		freeDrawPath.setStrokeWidth(2);
		freeDrawPath.setStroke(Color.RED);
		((AnchorPane) croppedImageView.getParent()).getChildren().add(freeDrawPath);

		// Sets the images of the buttons
		erasePerikymataButtonImage
				.setImage(new Image(this.getClass().getResource("/rsc/Eraser-icon.png").toExternalForm()));
		drawPerikymataButtonImage
				.setImage(new Image(this.getClass().getResource("/rsc/Pen-icon.png").toExternalForm()));
	}

	/**
	 * Handles the zooming in, redraws the image elements.
	 */
	@FXML
	private void zoomPlus() {
		croppedImageView.setFitHeight(croppedImageView.getFitHeight() * 1.25);
		croppedImageView.setFitWidth(croppedImageView.getFitWidth() * 1.25);
		reDrawElements();
	}

	/**
	 * Handles the zooming out, redraws the image elements.
	 */
	@FXML
	private void zoomMinus() {
		croppedImageView.setFitHeight(croppedImageView.getFitHeight() * 0.75);
		croppedImageView.setFitWidth(croppedImageView.getFitWidth() * 0.75);
		reDrawElements();

	}

//	/**
//	 * Handles the selection of the starting bound of the tooth.
//	 */
//	@FXML
//	private void selectStart() {
//		clearImageViewHandlers();
//		fullOriginalImage.setVisible(true);
//
//		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
//					xDecileStart = modifyBoundsLine(mouseEvent, lineDecileStart);
//					fullImage.setOnMouseClicked(null);
//					statusLabel.setText("Start point selected.");
//					fullOriginalImage.setVisible(false);
//					mainApp.getProject().setxDecileStart(xDecileStart);
//					mainApp.makeProjectXml();
//					if (xDecileStart != null && xDecileEnd != null) {
//						calculateDeciles();
//					}
//				}
//			}
//		};
//		statusLabel.setText("Selecting Start point.");
//		fullImage.setPickOnBounds(true);
//		fullImage.setOnMouseClicked(mouseHandler);
//
//	}
//
//	/**
//	 * Handles the selection of the Ending bound of the tooth.
//	 */
//	@FXML
//	private void selectEnd() {
//		clearImageViewHandlers();
//		fullOriginalImage.setVisible(true);
//		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
//					xDecileEnd = modifyBoundsLine(mouseEvent, lineDecileEnd);
//					fullImage.setOnMouseClicked(null);
//					statusLabel.setText("End point selected.");
//					fullOriginalImage.setVisible(false);
//					mainApp.getProject().setxDecileStart(xDecileEnd);
//					mainApp.makeProjectXml();
//					if (xDecileStart != null && xDecileEnd != null) {
//						calculateDeciles();
//					}
//				}
//			}
//		};
//		statusLabel.setText("Selecting End point.");
//		fullImage.setPickOnBounds(true);
//		fullImage.setOnMouseClicked(mouseHandler);
//
//	}

//	/**
//	 * Modifies the line to a new position defined by the coordinates of the
//	 * mouseEvent and returns the X coordinate on the real image.
//	 *
//	 * @param me
//	 *            Mouse event that shows the new position of the line.
//	 * @param line
//	 *            Line to draw or redraw.
//	 * @return x position relative to the real image.
//	 */
//	private double modifyBoundsLine(MouseEvent me, Line line) {
//		line.setStartX(new Double(me.getX()));
//		line.setEndX(new Double(me.getX()));
//		line.setStartY(0);
//		line.setEndY(new Double(fullImage.getFitHeight()));
//		double ratio = (fullImage.getImage().getWidth() / fullImage.getFitWidth());
//		return Double.valueOf(me.getX() * ratio);
//	}

	/**
	 * Clears the line that has been drawn to detect perikymata and clears the
	 * mouse handlers.
	 */
	@FXML
	private void clearLine() {
		clearImageViewHandlers();
		statusLabel.setText("Line cleared.");
		((AnchorPane) croppedImageView.getParent()).getChildren().removeAll(circles);
		circles.clear();
		peaksCoords.clear();

		this.freeDrawPathList.clear();
		freeDrawPath.getElements().clear();
	}

	/**
	 * Handles the selection of the bounds of the tooth.
	 */
	@FXML
	private void drawPath() {

		clearImageViewHandlers();
		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double ratio = (croppedImageView.getImage().getWidth() / croppedImageView.getFitWidth());
				if (mouseEvent.getButton().compareTo(MouseButton.SECONDARY) == 0) {
					statusLabel.setText("Finished drawing line.");
					clearImageViewHandlers();
					mainApp.getProject().setLinePath(freeDrawPathList);
					mainApp.makeProjectXml();
				} else if (freeDrawPath.getElements().isEmpty()
						&& (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED
								|| mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED)) {
					freeDrawPath.getElements().add(new MoveTo(mouseEvent.getX(), mouseEvent.getY()));
					freeDrawPathList.add(new MoveTo(mouseEvent.getX() * ratio, mouseEvent.getY() * ratio));
				} else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED
						|| mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
					freeDrawPath.getElements().add(new LineTo(mouseEvent.getX(), mouseEvent.getY()));
					freeDrawPathList.add(new LineTo(mouseEvent.getX() * ratio, mouseEvent.getY() * ratio));
					mainApp.getProject().setLinePath(freeDrawPathList);
					mainApp.makeProjectXml();

				}
				;
			}
		};
		statusLabel.setText("Drawing line, draw or click between two points. Right click to end.");
		croppedImageView.setPickOnBounds(true);
		croppedImageView.setOnMouseClicked(mouseHandler);
		croppedImageView.setOnMouseDragged(mouseHandler);
		croppedImageView.setOnMousePressed(mouseHandler);

	}

	/**
	 * Draws circles where perikymata are detected.
	 */
	private void drawPeaks() {
		((AnchorPane) croppedImageView.getParent()).getChildren().removeAll(circles);
		circles.clear();
		double ratio = (croppedImageView.getImage().getWidth() / croppedImageView.getFitWidth());
		for (int[] peak : peaksCoords) {
			Circle c = new Circle(peak[0] / ratio, peak[1] / ratio, 2);
			c.setStroke(Color.TEAL);
			c.setFill(Color.TEAL);
			((AnchorPane) croppedImageView.getParent()).getChildren().add(c);
			circles.add(c);
		}

	}

	/**
	 * Redraws the graphic interface objects, usually used when zooming.
	 */
	private void reDrawElements() {
		double ratio = (croppedImageView.getImage().getWidth() / croppedImageView.getFitWidth());
		// Draws the perikymata circles.
		if (!circles.isEmpty()) {
			drawPeaks();
		}

		// Draws the measure line
//		if (measure != null && measure.getStartMeasure() != null && measure.getEndMeasure() != null) {
//			measureLine.setStartX(measure.getStartMeasure()[0] / ratio);
//			measureLine.setStartY(measure.getStartMeasure()[1] / ratio);
//			measureLine.setEndX(measure.getEndMeasure()[0] / ratio);
//			measureLine.setEndY(measure.getEndMeasure()[1] / ratio);
//
//		}

		// Draws the deciles.
		if (xDecileStart != null) {
			lineDecileStart.setStartX(xDecileStart / ratio);
			lineDecileStart.setEndX(xDecileStart / ratio);
			lineDecileStart.setEndY(croppedImageView.getFitHeight());
		}
		if (xDecileEnd != null) {
			lineDecileEnd.setStartX(xDecileEnd / ratio);
			lineDecileEnd.setEndX(xDecileEnd / ratio);
			lineDecileEnd.setEndY(croppedImageView.getFitHeight());
		}

		if (xDecileStart != null && xDecileEnd != null) {
			for (int i = 0; i < 9; i++) {
				decilesLinesBetween[i].setStartX(decilesBetween[i] / ratio);
				decilesLinesBetween[i].setEndX(decilesBetween[i] / ratio);
				decilesLinesBetween[i].setEndY(croppedImageView.getFitHeight());
			}
		}

		// Redraws the free-Draw line.
		if (!freeDrawPathList.isEmpty()) {
			freeDrawPath.getElements().clear();
			freeDrawPath.getElements().add(new MoveTo(((MoveTo) freeDrawPathList.get(0)).getX() / ratio,
					((MoveTo) freeDrawPathList.get(0)).getY() / ratio));
			for (int i = 1; i < freeDrawPathList.size(); i++) {
				freeDrawPath.getElements().add(new LineTo(((LineTo) freeDrawPathList.get(i)).getX() / ratio,
						((LineTo) freeDrawPathList.get(i)).getY() / ratio));

			}
		}
	}

	/**
	 * Given a start and an end, divides the interval into 10 equal slices
	 * (deciles) and draws those slices over the image.
	 */
	private void calculateDeciles() {

		double min = xDecileStart, max = xDecileEnd;
		if (Double.compare(xDecileStart, xDecileEnd) > 0) {
			min = xDecileEnd;
			max = xDecileStart;
		}
		double sizeDecil = (max - min) / 10;

		decilesBetween[0] = min + sizeDecil;
		for (int i = 1; i < 9; i++) {
			decilesBetween[i] = decilesBetween[i - 1] + sizeDecil;
		}
		reDrawElements();

		mainApp.getProject().setxDecileStart(xDecileStart);
		mainApp.getProject().setxDecileEnd(xDecileEnd);
		mainApp.makeProjectXml();
	}

	/**
	 * Handler that calculates the localization of the perikymata over the drawn
	 * line and draws circles there.
	 */
	@FXML
	private void calculatePerikymata() {
		loading.setVisible(true);
		this.statusLabel.setText("Calculating Perikymata, this can take several minutes.");

		((AnchorPane) croppedImageView.getParent()).getChildren().removeAll(circles);
		circles.clear();
		peaksCoords.clear();
		if (!freeDrawPathList.isEmpty()) {
			Task<Void> task = new Task<Void>() {
				protected Void call() {
					mainApp.getRootLayout().setDisable(true);
					try {

						Platform.runLater(() -> statusLabel.setText("Calculating profile coords... 1/4"));
						List<int[]> profile = ProfileUtil.getProfilePixels(freeDrawPathList);
						Platform.runLater(() -> statusLabel.setText("Calculating profile intensity... 2/4"));
						List<Integer> intensity = ProfileUtil.getIntensityProfile(profile, mainApp);
						Platform.runLater(() -> statusLabel.setText("Finding perikymata... 3/4"));
						List<Integer> peaksIndexes = ProfileUtil.findLocalPeaks(intensity,
								(255 * thresholdSlider.valueProperty().get()) / thresholdSlider.getMax());
						for (Integer i : peaksIndexes) {
							peaksCoords.add(profile.get(i));
						}
						Platform.runLater(() -> statusLabel.setText("Drawing perikymata... 4/4"));
						mainApp.getProject().setPeaksCoords(peaksCoords);
						mainApp.makeProjectXml();
						Platform.runLater(() -> drawPeaks());
						Platform.runLater(() -> statusLabel.setText("Perikymata marking completed"));
					} catch (Exception e) {
						mainApp.getLogger().log(Level.SEVERE, "Error marking Perikymata.", e);
					} finally {
						loading.setVisible(false);
						mainApp.getRootLayout().setDisable(false);
					}
					return null;
				}
			};
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();

		} else {
			statusLabel.setText("Line has not been drawn");
		}
	}

	/**
	 * Clears the handlers of the imageview.
	 */
	private void clearImageViewHandlers() {
		croppedImageView.setOnMouseClicked(null);
		croppedImageView.setOnMouseDragged(null);
		croppedImageView.setOnMousePressed(null);
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * Also, sets the full Image. This is done here because when the method
	 * initialize is called, there is no reference to the mainapp.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		if (mainApp.getFullImage() != null) {
			//croppedImageView.setImage(mainApp.getFilteredImage());
			croppedImageView.setImage(mainApp.getCroppedImage());
			croppedImageView.setFitHeight(croppedImageView.getImage().getHeight());
			croppedImageView.setFitWidth(croppedImageView.getImage().getWidth());
			croppedImageView.setPreserveRatio(true);
//			fullOriginalImage.setImage(mainApp.getFullImage());
			freeDrawPathList.clear();
			if (mainApp.getProject().getLinePath() != null)
				freeDrawPathList.addAll(mainApp.getProject().getLinePath());
			if (mainApp.getProject().getPeaksCoords() != null) {
				peaksCoords = mainApp.getProject().getPeaksCoords();
				drawPeaks();
			}
			measure = mainApp.getProject().getMeasure();

			xDecileStart = mainApp.getProject().getxDecileStart();
			xDecileEnd = mainApp.getProject().getxDecileEnd();
			if (xDecileStart != null & xDecileEnd != null)
				calculateDeciles();

			this.reDrawElements();
		}
	}

//	/**
//	 * Handler that puts an event on the image to mark the start of the measure,
//	 * if start and end measures are set, line is drawn and measure dialog is
//	 * called.
//	 */
//	@FXML
//	private void measureStartHandler() {
//
//		clearImageViewHandlers();
//		fullOriginalImage.setVisible(true);
//		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
//					if (measure != null && measure.getStartMeasure() == null) {
//						measure.setStartMeasure(new double[2]);
//					}
//					measure.getStartMeasure()[0] = mouseEvent.getX() * getImageToImageViewRatio();
//					measure.getStartMeasure()[1] = mouseEvent.getY() * getImageToImageViewRatio();
//					fullImage.setOnMouseClicked(null);
//					statusLabel.setText("Start measure point selected.");
//					fullOriginalImage.setVisible(false);
//					if (measure.getStartMeasure() != null && measure.getEndMeasure() != null) {
//						measure();
//					}
//				}
//			}
//		};
//		statusLabel.setText("Selecting start point for the measure.");
//		fullImage.setPickOnBounds(true);
//		fullImage.setOnMouseClicked(mouseHandler);
//	}

//	/**
//	 * Handler that puts an event on the image to mark the end of the measure,
//	 * if start and end measures are set, line is drawn and measure dialog is
//	 * called.
//	 */
//	@FXML
//	private void measureEndHandler() {
//		clearImageViewHandlers();
//		fullOriginalImage.setVisible(true);
//		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
//					if (measure.getEndMeasure() == null) {
//						measure.setEndMeasure(new double[2]);
//					}
//					measure.getEndMeasure()[0] = mouseEvent.getX() * getImageToImageViewRatio();
//					measure.getEndMeasure()[1] = mouseEvent.getY() * getImageToImageViewRatio();
//					fullImage.setOnMouseClicked(null);
//					statusLabel.setText("End measure point selected.");
//					fullOriginalImage.setVisible(false);
//					if (measure.getStartMeasure() != null && measure.getEndMeasure() != null) {
//						measure();
//					}
//				}
//			}
//		};
//		statusLabel.setText("Selecting End point for the measure.");
//		fullImage.setPickOnBounds(true);
//		fullImage.setOnMouseClicked(mouseHandler);
//	}

//	/**
//	 * Draws the line between startMeasure and EndMeasure and shows a dialog
//	 * asking for the units and value of the measure.
//	 */
//	@FXML
//	private void measure() {
//
//		measureLine.setStartX(measure.getStartMeasure()[0] / getImageToImageViewRatio());
//		measureLine.setStartY(measure.getStartMeasure()[1] / getImageToImageViewRatio());
//		measureLine.setEndX(measure.getEndMeasure()[0] / getImageToImageViewRatio());
//		measureLine.setEndY(measure.getEndMeasure()[1] / getImageToImageViewRatio());
//
//		mainApp.getProject().getMeasure().setStartMeasure(measure.getStartMeasure());
//		mainApp.getProject().getMeasure().setEndMeasure(measure.getEndMeasure());
//
//		// Create the custom dialog.
//		Dialog<Pair<String, String>> dialog = new Dialog<>();
//		dialog.setTitle("Input the image measure unit and measure value.");
//		dialog.setHeaderText("Input the image measure unit and measure value.");
//
//		// Set the button types.
//		ButtonType doneButtonType = new ButtonType("Done", ButtonData.OK_DONE);
//		dialog.getDialogPane().getButtonTypes().addAll(doneButtonType, ButtonType.CANCEL);
//
//		// Create the username and password labels and fields.
//		GridPane grid = new GridPane();
//		grid.setHgap(10);
//		grid.setVgap(10);
//		grid.setPadding(new Insets(20, 150, 10, 10));
//
//		// TextField measureUnit = new TextField();
//		// measureUnit.setPromptText("Measure unit");
//		ObservableList<String> options = FXCollections.observableArrayList("cm", "mm", "µm", "nm");
//		final ComboBox<String> measureUnit = new ComboBox<>(options);
//		if (measure == null || measure.getMeasureUnit() == null)
//			measureUnit.setValue("mm");
//		else
//			measureUnit.setValue(measure.getMeasureUnit());
//		TextField measureValue = new TextField();
//		measureValue.setPromptText("Measure value");
//		if (measure != null && measure.getMeasureValue() != 0) {
//			measureValue.setText(Double.toString(measure.getMeasureValue()));
//		}
//		measureValue.setTextFormatter(new TextFormatter<String>(change -> {
//			if (change.getText().matches("[0-9]*(\\.)?[0-9]*")) {
//				if (change.getText().endsWith("."))
//					change.setText(change.getText() + "0");
//				return change;
//			}
//			return null;
//		}));
//
//		grid.add(new Label("Measure unit:"), 0, 0);
//		grid.add(measureUnit, 1, 0);
//		grid.add(new Label("Measure value:"), 0, 1);
//		grid.add(measureValue, 1, 1);
//
//		// Enable/Disable login button depending on whether a username was
//		// entered.
//		Node acceptButton = dialog.getDialogPane().lookupButton(doneButtonType);
//		acceptButton.setDisable(true);
//
//		// Do some validation (using the Java 8 lambda syntax).
//		measureValue.textProperty().addListener((observable, oldValue, newValue) -> {
//			acceptButton.setDisable(newValue.trim().isEmpty());
//		});
//
//		dialog.getDialogPane().setContent(grid);
//
//		// Request focus on the username field by default.
//		Platform.runLater(() -> measureUnit.requestFocus());
//
//		// Convert the result to a username-password-pair when the login button
//		// is clicked.
//		dialog.setResultConverter(dialogButton -> {
//			if (dialogButton == doneButtonType) {
//
//				return new Pair<>(measureUnit.getValue().toString(), measureValue.getText());
//			}
//			statusLabel.setText("Measure has not been changed.");
//			return null;
//		});
//
//		Optional<Pair<String, String>> result = dialog.showAndWait();
//
//		result.ifPresent(measureValues -> {
//			statusLabel.setText("Measure changed correctly.");
//			measure.setMeasureUnit(measureValues.getKey());
//			measure.setMeasureValue(Double.parseDouble(measureValues.getValue()));
//			mainApp.getProject().setMeasure(measure);
//			mainApp.makeProjectXml();
//		});
//	}

	/**
	 *
	 * @return the proportion between the original image and the imageview.
	 */
	private double getImageToImageViewRatio() {
		return croppedImageView.getImage().getWidth() / croppedImageView.getFitWidth();
	}

	/**
	 * Handler of a button that, on pressed, marks that there is a perikymata
	 * where the mouse is clicked.
	 */
	@FXML
	private void handleDrawPerikymata() {
		statusLabel.setText("Drawing Perikymata, right click to end.");
		clearImageViewHandlers();
		EventHandler<Event> h = evt -> {
			if (((MouseEvent) evt).getButton().compareTo(MouseButton.SECONDARY) == 0) {
				statusLabel.setText("Perikymata Hand-Marking ended.");
				clearImageViewHandlers();
			} else {
				peaksCoords.add(new int[] { (int) (((MouseEvent) evt).getX() * this.getImageToImageViewRatio()),
						(int) (((MouseEvent) evt).getY() * this.getImageToImageViewRatio()) });
				mainApp.getProject().setPeaksCoords(peaksCoords);
				mainApp.makeProjectXml();
				drawPeaks();
			}
		};
		croppedImageView.setOnMousePressed(h);
	}

	/**
	 * Handler that, on pressed, deletes marked perikymata on mouse click.
	 */
	@FXML
	private void handleErasePerikymata() {
		clearImageViewHandlers();
		statusLabel.setText("Click a perikymata circle to erase it.");
		EventHandler<Event> h = evt -> {
			Circle c = (Circle) evt.getSource();
			peaksCoords.remove(circles.indexOf(c));
			drawPeaks();
			mainApp.getProject().setPeaksCoords(peaksCoords);
			mainApp.makeProjectXml();
			handleErasePerikymata();

		};

		for (Circle c : circles) {
			c.addEventHandler(MouseEvent.MOUSE_CLICKED, h);
		}

	}

	/**
	 * Outputs a CSV file with the data of the perikymata.
	 */
	@FXML
	private void generateCsvFile() {
		if (xDecileStart == null) {
			statusLabel.setText("Start Decile is missing, cannot make CSV.");
		} else if (xDecileEnd == null) {
			statusLabel.setText("End Decile is missing, cannot make CSV.");
		} else if (measure == null || measure.getMeasureValue() == 0) {
			statusLabel.setText("Measure missing, cannot make CSV.");
		} else if (peaksCoords.isEmpty()) {
			statusLabel.setText("Perikymata not detected, cannot make CSV.");
		} else {
			try {
				CSVUtil.createCSV(mainApp, measure, peaksCoords, xDecileStart, xDecileEnd, decilesBetween);
				statusLabel.setText("CSV exported succesfully");
			} catch (IOException e) {
				statusLabel.setText("Error exporting CSV");
				mainApp.getLogger().log(Level.SEVERE, "Error while saving CSV.", e);
			}

		}
	}
}
