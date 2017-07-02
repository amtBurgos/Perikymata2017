package es.ubu.lsi.perikymata.vista;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.modelo.Measure;
import es.ubu.lsi.perikymata.util.CSVUtil;
import es.ubu.lsi.perikymata.util.ProfileUtilV2;
import es.ubu.lsi.perikymata.util.sockets.ClientSocket;
import es.ubu.lsi.perikymata.util.sockets.Request;
//import ij.io.Opener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.stage.Stage;

/**
 * Controller for the layout that is used to count perikyma.
 *
 * @author Sergio Chico Carrancio
 * @author Andres Miguel Teran
 */
public class PerikymataCountController {
	/**
	 * Checkbox to enable or disable lines detections.
	 */
	@FXML
	private CheckBox activateDetectionCheckbox;

	/**
	 * Toggle button for opening the advanced filter layout.
	 */
	@FXML
	private ToggleButton advancedFilterToggleBtn;

	/**
	 * VBox for the advanced filter layout.
	 */
	@FXML
	private VBox advancedFilterVBox;

	/**
	 * Circles drawn to show where perikymata are found.
	 */
	private List<Circle> circles = FXCollections.observableArrayList();

	/**
	 * Image view of the image used to calculate the perikymata.
	 */
	@FXML
	private ImageView croppedImageView;

	/**
	 * X coordinates of the deciles between the DecileStart and DecileEnd.
	 */
	private double[] decilesBetween = new double[9];

	/**
	 * Line objects of the deciles between DecileStart and DecileEnd
	 */
	private Line[] decilesLinesBetween = new Line[9];

	/**
	 * Dennoise slider for advanced filtering options.
	 */
	@FXML
	private Slider denoiseForceSlider;

	/**
	 * Toggle button for drawing a line free hand.
	 */
	@FXML
	private ToggleButton drawLineBtn;

	/**
	 * Image for the drawLineBtn (ToggleButton)
	 */
	@FXML
	private ImageView drawLineButtonImage;

	/**
	 * Image for draw perikymata button.
	 */
	@FXML
	private ImageView drawPerikymataButtonImage;

	/**
	 * Image for erase perikymata button.
	 */
	@FXML
	private ImageView erasePerikymataButtonImage;

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
	 * Left anchor pane inside border pane.
	 */
	@FXML
	private AnchorPane leftAnchorPane;

	/**
	 * End Line object drawn over the image.
	 */
	private Line lineDecileEnd;

	/**
	 * Start Line object drawn over the image.
	 */
	private Line lineDecileStart;

	/**
	 * Line gap slider for advanced filtering options.
	 */
	@FXML
	private Slider lineGapSlider;

	/**
	 * Loading gif.
	 */
	@FXML
	private ImageView loading;

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * Maximum angle slider for advanced filtering options.
	 */
	@FXML
	private Slider maxAngleSlider;

	/**
	 * Measure object with the coordinates and the value of the measure.
	 */
	private Measure measure;

	/**
	 * Minimum angle slider for advanced filtering options.
	 */
	@FXML
	private Slider minAngleSlider;

	/**
	 * Minimum line length slider for advanced filtering options.
	 */
	@FXML
	private Slider minLineLengthSlider;

	/**
	 * Coordinates of the detected perikymata.
	 */
	private List<int[]> peaksCoords = new ArrayList<>();

	/**
	 * Pane that contains all elements.
	 */
	@FXML
	private BorderPane perikymataCountPane;

	/**
	 * Perikymata orientation combobox for advanced filtering options.
	 */
	@FXML
	private ComboBox<String> perikymataOrientationCombobox;

	/**
	 * Small objects slider for advanced filtering options.
	 */
	@FXML
	private Slider smallObjectSlider;

	/**
	 * End X coordinate of the deciles on the original image.
	 */
	private Double xDecileEnd = null;

	/**
	 * Start X coordinate of the deciles on the original image.
	 */
	private Double xDecileStart = null;

	/**
	 * Zoom minus button image.
	 */
	@FXML
	private ImageView zoomMinusBtnImage;

	/**
	 * Zoom plus button image.
	 */
	@FXML
	private ImageView zoomPlusBtnImage;

	/**
	 * Alternates the image view between the filtered image and the original
	 * cropped image with the filtered overlapped.
	 */
	@FXML
	private void alternateFilteredImages() {
		if (mainApp.getFilteredOverlappedImage() != null && mainApp.getFilteredImage() != null) {
			if (croppedImageView.getImage().equals(mainApp.getFilteredImage())) {
				croppedImageView.setImage(mainApp.getFilteredOverlappedImage());
			} else {
				croppedImageView.setImage(mainApp.getFilteredImage());
			}
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
			window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
			alert.setTitle("Error");
			alert.setHeaderText("Can not alternate filtered images");
			alert.setContentText("Filtered images unavailable.");
			alert.showAndWait();
		}
	}

	/**
	 * Applies the advanced custom filter.
	 */
	@FXML
	private void applyAdvancedFilter() {
		handleFilter();
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
		// Check if image has been filtered
		if (mainApp.getFilteredImage() == null) {
			filterNotAppliedAlert();
		} else {
			loading.setVisible(true);
			((AnchorPane) croppedImageView.getParent()).getChildren().removeAll(circles);
			circles.clear();
			peaksCoords.clear();
			if (!freeDrawPathList.isEmpty()) {
				Task<Void> task = new Task<Void>() {
					protected Void call() {
						mainApp.getRootLayout().setDisable(true);
						try {
							List<int[]> profilePixels = ProfileUtilV2.getProfilePixels(freeDrawPathList);
							BufferedImage filteredImg = SwingFXUtils.fromFXImage(mainApp.getFilteredImage(), null);
							List<int[]> redPixels = ProfileUtilV2.findRedPixels(profilePixels, filteredImg);
							List<int[]> redPixelsClean = ProfileUtilV2.deleteClosePixels(redPixels,
									ProfileUtilV2.DEFAULT_MIN_COORD_DISTANCE);
							peaksCoords.clear();
							peaksCoords.addAll(redPixelsClean);

							mainApp.getProject().setPeaksCoords(peaksCoords);
							mainApp.makeProjectXml();
							Platform.runLater(() -> drawPeaks());
						} catch (Exception e) {
							mainApp.getLogger().log(Level.SEVERE, "Error marking Perikymata.", e);
							Platform.runLater(() -> {
								Alert alert = new Alert(Alert.AlertType.ERROR);
								alert.setTitle("Error calculating perikymata");
								alert.setHeaderText("Can not count perikymata\n");
								alert.setContentText("Can not count perikymata.");
								alert.showAndWait();
							});
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
				loading.setVisible(false);
				Alert alert = new Alert(Alert.AlertType.ERROR);
				Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
				window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
				alert.setTitle("Error calculating perikymata");
				alert.setHeaderText("Line has not been drawn.\n");
				alert.setContentText("Line has not been drawn");
				alert.showAndWait();
			}
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
	 * Clears the line that has been drawn to detect perikymata and clears the
	 * mouse handlers.
	 */
	@FXML
	private void clearLine() {
		if (!drawLineBtn.isSelected()) {
			clearImageViewHandlers();
			croppedImageView.setCursor(Cursor.DEFAULT);
		}
		((AnchorPane) croppedImageView.getParent()).getChildren().removeAll(circles);
		circles.clear();
		peaksCoords.clear();
		this.freeDrawPathList.clear();
		freeDrawPath.getElements().clear();
		mainApp.makeProjectXml();
	}

	/**
	 * Handles the selection of the bounds of the tooth.
	 */
	@FXML
	private void drawPath() {
		if (mainApp.getFilteredImage() == null) {
			filterNotAppliedAlert();
		} else {
			if (drawLineBtn.isSelected()) {
				EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent mouseEvent) {
						double ratio = (croppedImageView.getImage().getWidth() / croppedImageView.getFitWidth());
						if (mouseEvent.getButton().compareTo(MouseButton.SECONDARY) == 0) {
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
				croppedImageView.setPickOnBounds(true);
				croppedImageView.setOnMouseClicked(mouseHandler);
				croppedImageView.setOnMouseDragged(mouseHandler);
				croppedImageView.setOnMousePressed(mouseHandler);
				croppedImageView.setCursor(Cursor.CROSSHAIR);
			} else {
				clearImageViewHandlers();
				croppedImageView.setCursor(Cursor.DEFAULT);
			}
		}

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
			c.setStroke(Color.CHARTREUSE);
			c.setFill(Color.CHARTREUSE);
			((AnchorPane) croppedImageView.getParent()).getChildren().add(c);
			circles.add(c);
		}

	}

	/**
	 * Enable or disable the lines detection options at advanced filtering.
	 */
	@FXML
	private void enableLineDetectionCheckbox() {
		if (activateDetectionCheckbox.isSelected()) {
			minLineLengthSlider.setDisable(false);
			lineGapSlider.setDisable(false);
			minAngleSlider.setDisable(false);
			maxAngleSlider.setDisable(false);
		} else {
			minLineLengthSlider.setValue(30.0);
			minLineLengthSlider.setDisable(true);
			lineGapSlider.setValue(16.0);
			lineGapSlider.setDisable(true);
			minAngleSlider.setValue(-0.3);
			minAngleSlider.setDisable(true);
			maxAngleSlider.setValue(0.3);
			maxAngleSlider.setDisable(true);
		}

	}

	/**
	 * Throws an alert alerting the filter has not been applied.
	 */
	private void filterNotAppliedAlert() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
		alert.setTitle("Error");
		alert.setHeaderText("Filter not applied");
		alert.setContentText("Filter not applied to the image.");
		alert.showAndWait();
	}

	/**
	 * Outputs a CSV file with the data of the perikymata.
	 */
	@FXML
	private void generateCsvFile() {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
		alert.setTitle("Error recovering CSV information");
		alert.setHeaderText("Cannot make CSV.");
		try {
			if (mainApp.getFilteredImage() == null) {
				filterNotAppliedAlert();
			} else if (measure == null || measure.getMeasureValue() == 0.0) {
				alert.setContentText("Measure missing.\nBack to Rotation & Crop.");
			} else if (peaksCoords.isEmpty()) {
				alert.setContentText("Perikymata not detected, cannot make CSV.");
			} else {
				CSVUtil.createCSV(mainApp, measure, peaksCoords, xDecileStart, xDecileEnd, decilesBetween);
				alert.setAlertType(AlertType.INFORMATION);
				alert.setTitle("CVS exported successfully");
				alert.setHeaderText("CSV exported succesfully");
				alert.setContentText("CSV exported succesfully");
			}
		} catch (IOException e) {
			mainApp.getLogger().log(Level.SEVERE, "Error while saving CSV.", e);
			alert.setTitle("Error exporting CSV");
			alert.setHeaderText("Error exporting CSV");
			alert.setContentText("Error exporting CSV");
		} finally {
			alert.showAndWait();
		}
	}

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
		clearImageViewHandlers();
		EventHandler<Event> h = evt -> {
			if (((MouseEvent) evt).getButton().compareTo(MouseButton.SECONDARY) == 0) {
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
	 * Send a filter request to python application, waits a response and load
	 * the filtered image.
	 */
	@FXML
	private void handleFilter() {
		// Throw new thread for communication with python server
		loading.setVisible(true);
		perikymataCountPane.setDisable(true);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientSocket client = new ClientSocket();
					String croppedImagePath = mainApp.getProjectPath() + File.separator + "Cropped_Image"
							+ File.separator + "Cropped_Image.png";
					String savePath = mainApp.getProjectPath() + File.separator + "Cropped_Image" + File.separator
							+ "Filtered_Image.png";
					String savePathOverlapped = mainApp.getProjectPath() + File.separator + "Cropped_Image"
							+ File.separator + "FilteredOverlapped_Image.png";

					// Diferent request depending if the advanced filter is open
					Request request = null;
					if (advancedFilterToggleBtn.isSelected()) {
						// Advanced request

						int detectLinesOrNot = activateDetectionCheckbox.isSelected() == true ? 1 : 0;
						int minLineLength = (int) minLineLengthSlider.getValue();
						int lineGap = (int) lineGapSlider.getValue();
						double minAngle = minAngleSlider.getValue();
						double maxAngle = maxAngleSlider.getValue();
						int smallObjectLenght = (int) smallObjectSlider.getValue();
						int kernel = perikymataOrientationCombobox.getSelectionModel().getSelectedIndex();
						double denoiseWeigh = denoiseForceSlider.getValue();

						// build request
						request = new Request(Request.ADVANCED_FILTER, croppedImagePath, savePath, savePathOverlapped,
								detectLinesOrNot, denoiseWeigh, kernel, minAngle, maxAngle, minLineLength, lineGap,
								smallObjectLenght);
					} else {
						// Default request
						request = new Request(Request.DEFAULT_FILTER, croppedImagePath, savePath, savePathOverlapped);
					}
					client.send(request);
					String response = client.receive();
					if (response.equals("OK")) {
						BufferedImage filtered = ImageIO.read(new File(mainApp.getProjectPath() + File.separator
								+ "Cropped_Image" + File.separator + "Filtered_Image.png"));

						mainApp.setFilteredImage(SwingFXUtils.toFXImage(filtered, null));
						croppedImageView.setImage(mainApp.getFilteredImage());
						BufferedImage overlapped = ImageIO.read(new File(mainApp.getProjectPath() + File.separator
								+ "Cropped_Image" + File.separator + "FilteredOverlapped_Image.png"));

						mainApp.setFilteredOverlappedImage(SwingFXUtils.toFXImage((BufferedImage) overlapped, null));
					} else {
						throw new Exception("Error in server during filtering.");
					}
					client.close();
				} catch (ConnectException e) {
					mainApp.getLogger().log(Level.SEVERE, "Exception occur filtering image. Server not running.", e);
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error filtering image");
						alert.setHeaderText("Server not running.\n");
						alert.setContentText("Can't filter image. Server not running.");
						alert.showAndWait();
					});
				} catch (Exception e) {
					mainApp.getLogger().log(Level.SEVERE, "Exception occur filtering image.", e);
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error filtering image");
						alert.setHeaderText("Can't filter image.\n");
						alert.setContentText("Can't filter image");
						alert.showAndWait();
					});
				} finally {
					Platform.runLater(() -> {
						loading.setVisible(false);
						perikymataCountPane.setDisable(false);
					});

				}

			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Initializes the Javafx components.
	 */
	@FXML
	private void initialize() {
		// Loads loading gif.
		loading.setImage(new Image(this.getClass().getResource("/rsc/482.gif").toExternalForm()));
		loading.setVisible(false);

		// Sets the parameters of the decile lines.
		lineDecileStart = new Line();
		lineDecileStart.setStroke(Color.CORNFLOWERBLUE);
		((AnchorPane) croppedImageView.getParent()).getChildren().add(lineDecileStart);
		lineDecileEnd = new Line();
		lineDecileEnd.setStroke(Color.CORNFLOWERBLUE);
		((AnchorPane) croppedImageView.getParent()).getChildren().add(lineDecileEnd);

		// Sets the images of the buttons
		erasePerikymataButtonImage
				.setImage(new Image(this.getClass().getResource("/rsc/Eraser-icon.png").toExternalForm()));
		drawPerikymataButtonImage
				.setImage(new Image(this.getClass().getResource("/rsc/Pen-icon.png").toExternalForm()));
		drawLineButtonImage
				.setImage(new Image(this.getClass().getResource("/rsc/Editing-Line-icon.png").toExternalForm()));
		zoomPlusBtnImage.setImage(new Image(this.getClass().getResource("/rsc/Zoom-Plus-icon.png").toExternalForm()));
		zoomMinusBtnImage.setImage(new Image(this.getClass().getResource("/rsc/Zoom-Minus-icon.png").toExternalForm()));

		// Inflate advanced options combobox
		//perikymataOrientationCombobox.setItems(FXCollections.observableArrayList("North", "North-west", "North-east"));
		perikymataOrientationCombobox.setItems(FXCollections.observableArrayList("Vertical", "Vertical Right", "Vertical Left",
																				"Vertical 2", "Vertical Right 2", "Vertical Left 2"));
		perikymataOrientationCombobox.getSelectionModel().selectFirst();
	}

	/**
	 * Opens and closes the advanced filter layout.
	 */
	@FXML
	private void openCloseAdvancedFilter() {
		if (advancedFilterToggleBtn.isSelected()) {
			leftAnchorPane.setPrefWidth(330.0);
			advancedFilterVBox.setVisible(true);
			// Open advanced filter layout
		} else {
			// Close advanced filter layout
			advancedFilterVBox.setVisible(false);
			leftAnchorPane.setPrefWidth(1.0);
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
	 * Resets default values in advanced filtering options.
	 */
	@FXML
	private void resetAdvancedValues() {
		minLineLengthSlider.setValue(30.0);
		lineGapSlider.setValue(16.0);
		minAngleSlider.setValue(-0.3);
		maxAngleSlider.setValue(0.3);
		smallObjectSlider.setValue(30.0);
		perikymataOrientationCombobox.getSelectionModel().selectFirst();
		denoiseForceSlider.setValue(0.5);
	}

	/**
	 * Resets all view and parameters.
	 *
	 * @param event
	 */
	@FXML
	private void resetView() {
		try {
			clearLine();
			clearImageViewHandlers();
			croppedImageView.setCursor(Cursor.DEFAULT);
			drawLineBtn.setSelected(false);
			croppedImageView.setImage(mainApp.getCroppedImage());
			mainApp.setFilteredImage(null);
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
			window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
			alert.setTitle("Error");
			alert.setHeaderText("Error resetting view.\n");
			alert.setContentText("Error resetting view.");
			alert.showAndWait();
		}
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * Also, sets the full Image. This is done here because when the method
	 * initialize is called, there is no reference to the mainapp.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		drawLineBtn.setSelected(false);
		croppedImageView.setImage(null);
		this.mainApp = mainApp;
		if (mainApp.getFullImage() != null) {

			if (mainApp.getFilteredImage() != null) {
				croppedImageView.setImage(mainApp.getFilteredImage());
			} else {
				croppedImageView.setImage(mainApp.getCroppedImage());
			}
			croppedImageView.setFitHeight(croppedImageView.getImage().getHeight());
			croppedImageView.setFitWidth(croppedImageView.getImage().getWidth());
			croppedImageView.setPreserveRatio(true);

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
			if (xDecileStart != null & xDecileEnd != null) {
				calculateDeciles();
			}
			if (mainApp.getFilteredImage() == null) {
				clearLine();
			}
			reDrawElements();

			// Hide advanced options
			advancedFilterToggleBtn.setSelected(false);
			advancedFilterVBox.setVisible(false);
			leftAnchorPane.setPrefWidth(20.0);
		}
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

	/**
	 * Handles the zooming in, redraws the image elements.
	 */
	@FXML
	private void zoomPlus() {
		croppedImageView.setFitHeight(croppedImageView.getFitHeight() * 1.25);
		croppedImageView.setFitWidth(croppedImageView.getFitWidth() * 1.25);
		reDrawElements();
	}
}
