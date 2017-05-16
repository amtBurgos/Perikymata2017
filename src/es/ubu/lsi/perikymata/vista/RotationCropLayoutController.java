package es.ubu.lsi.perikymata.vista;

/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.modelo.Measure;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Pair;

/**
 * Control for the rotation and crop view.
 *
 * @author Andres Miguel Teran
 *
 */
public class RotationCropLayoutController {

	/**
	 * Area selector button.
	 */
	@FXML // fx:id="areaSelectorBtn"
	private ToggleButton areaSelectorBtn; // Value injected by FXMLLoader

	/**
	 * Image for the area selector button.
	 */
	@FXML // fx:id="areaSelectorBtnImage"
	private ImageView areaSelectorBtnImage; // Value injected by FXMLLoader

	/**
	 * Crop button.
	 */
	@FXML // fx:id="cropBtn"
	private Button cropBtn; // Value injected by FXMLLoader

	/**
	 * Image for the crop button.
	 */
	@FXML // fx:id="cropBtnImage"
	private ImageView cropBtnImage; // Value injected by FXMLLoader

	/**
	 * List with the points to create a rectangle.
	 */
	private Point2D.Double[] croppingPoints;

	/**
	 * image cropped.
	 */
	// private WritableImage imageCropped;

	/**
	 * Flag true if the image has been cropped.
	 */
	private boolean isCropped;

	/**
	 * Full image.
	 */
	private BufferedImage img;

	/**
	 * Auxiliary image, used to reset the previewImage when reset button is
	 * pressed. It is the original full image and does not change.
	 */
	private BufferedImage imgAux;

	/**
	 * Input for the degrees rotation.
	 */
	@FXML // fx:id="inputDegrees"
	private TextField inputDegrees; // Value injected by FXMLLoader

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * Measure object with the coordinates and the value of the measure.
	 */
	private Measure measure;

	/**
	 * Drawn line from startMeasure to endMeasure.
	 */
	private Line measureLine;

	/**
	 * Toggle button for the measure line.
	 */
	@FXML
	private ToggleButton measureLineBtn;

	/**
	 * Customs mouse event handler for previewImage.
	 */
	private EventHandler<MouseEvent> mousePressedHandler, mouseDraggedHandler, mouseReleasedHandler;

	/**
	 * Imageview of the original image.
	 */
	@FXML
	private ImageView originalImagePreview;

	/**
	 * Customs mouse event handler for original image view.
	 */
	private EventHandler<MouseEvent> originalMousePressedHandler, originalMouseDraggedHandler,
			originalMouseReleasedHandler;

	/**
	 * Pane for the previewImage.
	 */
	private Pane pane;

	/**
	 * Preview image.
	 */
	@FXML // fx:id="previewImage"
	private ImageView previewImage; // Value injected by FXMLLoader

	/**
	 * Rectangle for crop.
	 */
	private Rectangle rect;

	/**
	 * Return button.
	 */
	@FXML // fx:id="returnBtn"
	private Button returnBtn; // Value injected by FXMLLoader

	/**
	 * Rotation in radians for the image.
	 */
	private Double rotationRadians;

	/**
	 * Slider for rotation.
	 */
	@FXML // fx:id="rotationSlider"
	private Slider rotationSlider; // Value injected by FXMLLoader

	/**
	 * Save and continue button.
	 */
	@FXML // fx:id="saveAndContinueBtn"
	private Button saveAndContinueBtn; // Value injected by FXMLLoader

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		areaSelectorBtnImage
				.setImage(new Image(this.getClass().getResource("/rsc/White-Square-icon.png").toExternalForm()));
		cropBtnImage.setImage(new Image(this.getClass().getResource("/rsc/Crop-2-icon.png").toExternalForm()));
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());

		// the original preview will not be visible at initialization
		originalImagePreview.setVisible(false);
		originalImagePreview.fitHeightProperty().bind(((Pane) originalImagePreview.getParent()).heightProperty());
		originalImagePreview.fitWidthProperty().bind(((Pane) originalImagePreview.getParent()).widthProperty());

		rotationRadians = 0.0;
		croppingPoints = new Point2D.Double[2];
		pane = (Pane) previewImage.getParent();
		pane.setStyle("-fx-background-color: #000000;");
		rect = null;
		isCropped = false;
		initMouseEventHandler();
	}

	/**
	 * Returns ratio between the image and its image view container.
	 *
	 * @param imageView
	 *            image view which contains the image
	 * @return ratio
	 */
	private double getImageToImageViewRatio(ImageView imageView) {
		return imageView.getBoundsInParent().getWidth() / imageView.getImage().getWidth();
	}

	/**
	 * Handle the crop feature, load the image cropped and save a copy to disk.
	 */
	@FXML
	private void handleCrop() {
		try {
			// If the user has selected an area to crop
			if (rect != null) {
				// Only needed X or Y because of the 'preservate ratio' check-in
				// in the fxml file
				Double ratio = getImageToImageViewRatio(previewImage);

				// Translate local coordinates of the area selector rectangle to
				// image pixel
				Point2D.Double start = new Point2D.Double(croppingPoints[0].x / ratio, croppingPoints[0].y / ratio);
				Point2D.Double end = new Point2D.Double(croppingPoints[1].x / ratio, croppingPoints[1].y / ratio);

				// Get image and apply crop area
				PixelReader pReader = previewImage.getImage().getPixelReader();
				WritableImage imageCropped = new WritableImage(pReader, (int) start.x, (int) start.y,
						(int) (end.x - start.x), (int) (end.y - start.y));

				previewImage.setImage(imageCropped);
				img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
				isCropped = true;
				removeRectanglesFromView();
			}
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur cropping image.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error cropping image");
			alert.setHeaderText("Can't crop image.\n");
			alert.setContentText("Can't crop image");
			alert.showAndWait();
		}

	}

	/**
	 * Handles the input for the rotation.
	 */
	@FXML
	private void handleInputDegrees(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			String input = inputDegrees.getText();
			if (validateInputDegrees(input)) {
				try {
					Double degrees = Double.parseDouble(input);
					rotationRadians = Math.toRadians(degrees);
					AffineTransform transform = new AffineTransform();
					transform.rotate(rotationRadians, img.getWidth() / 2, img.getHeight() / 2);
					AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
					Image i = SwingFXUtils.toFXImage(op.filter(img, null), null);
					previewImage.setImage(i);
					rotationSlider.setValue(degrees);
				} catch (Exception e) {
					mainApp.getLogger().log(Level.SEVERE, "Exception occur rotating image.", e);
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Error rotating image");
					alert.setHeaderText("Can't rotate image.\n");
					alert.setContentText("Can't rotate image");
					alert.showAndWait();
				}
			} else {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Can't rotate image");
				alert.setHeaderText("Invalid input.\n");
				alert.setContentText("Please, insert a number between -40.0 and 40.0.");
				alert.showAndWait();
			}
		} else {
			event.consume();
		}
	}

	/**
	 * Handles the measure.
	 */
	@FXML
	private void handleMeasure() {
		if (measureLineBtn.isSelected()) {
			originalImagePreview.setCursor(Cursor.CROSSHAIR);
			previewImage.setVisible(false);
			originalImagePreview.setVisible(true);
			removeRectanglesFromView();
			originalImagePreview.addEventHandler(MouseEvent.MOUSE_PRESSED, originalMousePressedHandler);
			originalImagePreview.addEventHandler(MouseEvent.MOUSE_DRAGGED, originalMouseDraggedHandler);
			originalImagePreview.addEventHandler(MouseEvent.MOUSE_RELEASED, originalMouseReleasedHandler);
		} else {
			previewImage.setVisible(true);
			originalImagePreview.setVisible(false);
			originalImagePreview.setCursor(Cursor.DEFAULT);
			removeLinesFromView();
			originalImagePreview.removeEventHandler(MouseEvent.MOUSE_PRESSED, originalMousePressedHandler);
			originalImagePreview.removeEventHandler(MouseEvent.MOUSE_DRAGGED, originalMouseDraggedHandler);
			originalImagePreview.removeEventHandler(MouseEvent.MOUSE_RELEASED, originalMouseReleasedHandler);
		}
	}

	/**
	 * Handles the units of the measure.
	 */
	@FXML
	private void handleMeasureUnits() {

		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Input the image measure unit and measure value.");
		dialog.setHeaderText("Input the image measure unit and measure value.");

		// Set the button types.
		ButtonType doneButtonType = new ButtonType("Done", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(doneButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		ObservableList<String> options = FXCollections.observableArrayList("cm", "mm", "µm", "nm");
		final ComboBox<String> measureUnit = new ComboBox<>(options);
		if (measure == null || measure.getMeasureUnit() == null)
			measureUnit.setValue("mm");
		else
			measureUnit.setValue(measure.getMeasureUnit());
		TextField measureValue = new TextField();
		measureValue.setPromptText("Measure value");
		if (measure != null && measure.getMeasureValue() != 0) {
			measureValue.setText(Double.toString(measure.getMeasureValue()));
		}
		measureValue.setTextFormatter(new TextFormatter<String>(change -> {
			if (change.getText().matches("[0-9]*(\\.)?[0-9]*")) {
				if (change.getText().endsWith("."))
					change.setText(change.getText() + "0");
				return change;
			}
			return null;
		}));

		grid.add(new Label("Measure unit:"), 0, 0);
		grid.add(measureUnit, 1, 0);
		grid.add(new Label("Measure value:"), 0, 1);
		grid.add(measureValue, 1, 1);

		// Enable/Disable login button depending on whether a username was
		// entered.
		Node acceptButton = dialog.getDialogPane().lookupButton(doneButtonType);
		acceptButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		measureValue.textProperty().addListener((observable, oldValue, newValue) -> {
			acceptButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> measureUnit.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == doneButtonType) {

				return new Pair<>(measureUnit.getValue().toString(), measureValue.getText());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();

		result.ifPresent(measureValues -> {
			measure.setMeasureUnit(measureValues.getKey());
			measure.setMeasureValue(Double.parseDouble(measureValues.getValue()));
			mainApp.getProject().setMeasure(measure);
			mainApp.makeProjectXml();
		});
	}

	/**
	 * Resets the view of the image to zero degrees rotation and original image.
	 */
	@FXML
	private void handleReset() {
		try {
			previewImage.setVisible(true);
			originalImagePreview.setVisible(false);
			removeRectanglesFromView();
			removeLinesFromView();
			previewImage.setImage(SwingFXUtils.toFXImage(imgAux, null));
			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			isCropped = false;
			rotationSlider.setValue(0.0);
			inputDegrees.setText("0.0");

			if (areaSelectorBtn.isSelected()) {
				previewImage.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
				previewImage.setCursor(Cursor.DEFAULT);
				areaSelectorBtn.setSelected(false);
			}

			if (measureLineBtn.isSelected()) {
				originalImagePreview.removeEventHandler(MouseEvent.MOUSE_PRESSED, originalMousePressedHandler);
				originalImagePreview.removeEventHandler(MouseEvent.MOUSE_DRAGGED, originalMouseDraggedHandler);
				originalImagePreview.removeEventHandler(MouseEvent.MOUSE_RELEASED, originalMouseReleasedHandler);
				measureLineBtn.setSelected(false);
				originalImagePreview.setCursor(Cursor.DEFAULT);
			}
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur resetting view", e);
		}
	}

	/**
	 * Handles the rotation feature with the slider item.
	 */
	@FXML
	private void handleRotation() {
		try {
			rotationRadians = Math.toRadians(rotationSlider.getValue());
			AffineTransform transform = new AffineTransform();
			transform.rotate(rotationRadians, img.getWidth() / 2, img.getHeight() / 2);
			AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			Image i = SwingFXUtils.toFXImage(op.filter(img, null), null);
			previewImage.setImage(i);
			inputDegrees.setText(String.format(Locale.US, "%.2f%n", rotationSlider.getValue()));
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur rotating image.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error rotating image");
			alert.setHeaderText("Can't rotate image.\n");
			alert.setContentText("Can't rotate image");
			alert.showAndWait();
		}
	}

	/**
	 * Goes to the next stage, perikymata counter.
	 */
	@FXML
	private void handleSaveAndContinue() {
		try {
			LinkedList<Integer> validateOperations = validateOperations();
			if (validateOperations == null) {
				saveAll();
				nextStage();
			} else {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Incompleted Operations");
				alert.setContentText("Continue without saving?");
				ButtonType continueBtn = new ButtonType("Continue");
				ButtonType cancelBtn = new ButtonType("Cancel");
				alert.getButtonTypes().setAll(cancelBtn, continueBtn);
				String header = "";
				if (validateOperations.contains(4)) {
					header += "- Dental crown is not cropped.\n";
				}
				if (validateOperations.contains(3)) {
					header += "- Measure line is not set.\n";
				}
				if (validateOperations.contains(2)) {
					header += "- Measure value is not set.\n";
				}
				if (validateOperations.contains(1)) {
					header += "- Measure unit is not set.\n";
				}
				alert.setHeaderText(header);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == continueBtn) {
					saveAll();
					nextStage();
				} else {
					alert.close();
				}
			}
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception saving project and loading next stage.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error loading next stage.");
			alert.setHeaderText("Can't load next stage.\n");
			alert.setContentText("Can't load next stage");
			alert.showAndWait();
		}
	}

	/**
	 * Handles the opening of the area selector, setting up event handlers for
	 * the image preview node.
	 */
	@FXML
	private void handleSelectorArea() {
		try {
			if (areaSelectorBtn.isSelected()) {
				// Add listener to start a rectangle on the image
				previewImage.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
				previewImage.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
				previewImage.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);

				previewImage.setCursor(Cursor.CROSSHAIR);
			} else {
				// Remove listeners and possibles rectangles areas
				previewImage.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);

				previewImage.setCursor(Cursor.DEFAULT);
				removeRectanglesFromView();
			}
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur opening area selectore.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error opening area selector.");
			alert.setHeaderText("Can't open area selector.\n");
			alert.setContentText("Can't open area selector.");
			alert.showAndWait();
		}
	}

	/**
	 * Initializes the mouse event handler.
	 */
	private void initMouseEventHandler() {

		// Handler for press event
		mousePressedHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// The interaction will be with the left button of the mouse
				if (!event.isSecondaryButtonDown()) {
					// Discard previous rectangles in the pane
					removeRectanglesFromView();
					croppingPoints[0] = new Point2D.Double(event.getX(), event.getY());
					rect = new Rectangle(croppingPoints[0].x, croppingPoints[0].y, 0, 0);
					rect.setStroke(Color.WHITE);
					rect.setStrokeWidth(0.6);
					rect.setStrokeLineCap(StrokeLineCap.SQUARE);
					rect.setFill(Color.WHITE.deriveColor(0, 0, 1, 0.4));
					pane.getChildren().add(rect);
				}
			}
		};

		// Handler for drag event
		mouseDraggedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				rect.setWidth(Math.abs(event.getX() - croppingPoints[0].x));
				rect.setHeight(Math.abs(event.getY() - croppingPoints[0].y));
			}

		};

		// Handler triggered when the mouse button is released
		mouseReleasedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Save the final coordinates
				croppingPoints[1] = new Point2D.Double(event.getX(), event.getY());
			}
		};

		// Handler for the original image view
		originalMousePressedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				removeLinesFromView();
				double x = event.getX();
				double y = event.getY();
				Double ratio = getImageToImageViewRatio(originalImagePreview);
				measure.setStartMeasure(new double[] { x / ratio, y / ratio });
				measureLine = new Line(x, y, x, y);
				measureLine.setStroke(Color.RED);
				measureLine.setStrokeWidth(4);
				pane.getChildren().add(measureLine);
			}

		};

		originalMouseDraggedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				measureLine.setEndX(event.getX());
				measureLine.setEndY(event.getY());
			}

		};

		originalMouseReleasedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Double ratio = getImageToImageViewRatio(originalImagePreview);
				measure.setEndMeasure(new double[] { event.getX() / ratio, event.getY() / ratio });
				mainApp.getProject().getMeasure().setStartMeasure(measure.getStartMeasure());
				mainApp.getProject().getMeasure().setEndMeasure(measure.getEndMeasure());
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Measure saved.\n");
				alert.setContentText("Measure line length successfully saved.");
				alert.showAndWait();
				if (mainApp.getProject().getMeasure().getMeasureUnit() == null) {
					handleMeasureUnits();
				}
			}
		};
	}

	/**
	 * Goes to the next stage.
	 */
	private void nextStage() {
		mainApp.showPerikymataCount();
	}

	/**
	 * Saves the cropped image.
	 *
	 * @throws IOException
	 *             IOException
	 */
	private void saveAll() throws IOException {
		removeLinesFromView();
		removeRectanglesFromView();
		// Save cropped image to disk, and load it in the image
		// preview
		File outputfile = new File(
				mainApp.getProjectPath() + File.separator + "Cropped_Image" + File.separator + "Cropped_image.png");
		outputfile.setWritable(true, false);
		ImageIO.write(img, "png", outputfile);

		// Save where the deciles start and end in the image
		mainApp.getProject().setxDecileStart(0d);
		mainApp.getProject().setxDecileEnd(previewImage.getImage().getWidth());
		mainApp.setCroppedImage(previewImage.getImage());
	}

	/**
	 * Goes to the previous stage, image selection and stitching.
	 */
	@FXML
	private void handleReturn() {
		try {
			handleReset();
			mainApp.showImageSelection();
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception loading previous stage.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error loading previous stage.");
			alert.setHeaderText("Can't load previous stage.\n");
			alert.setContentText("Can't load previous stage");
			alert.showAndWait();
		}

	}

	/**
	 * Removes red lines from the origal image view.
	 */
	private void removeLinesFromView() {
		pane.getChildren().removeIf(new Predicate<Object>() {
			@Override
			public boolean test(Object o) {
				boolean isInstance = false;
				if (o instanceof Line) {
					isInstance = true;
				}
				return isInstance;
			}
		});
	}

	/**
	 * Removes all rectangles from the image preview.
	 */
	private void removeRectanglesFromView() {
		pane.getChildren().removeIf(new Predicate<Object>() {
			@Override
			public boolean test(Object o) {
				boolean isInstance = false;
				if (o instanceof Rectangle) {
					isInstance = true;
				}
				return isInstance;
			}
		});
	}

	// private <T extends Node> void removeRectanglesFromView(T object) {
	// pane.getChildren().removeIf(new Predicate<T>() {
	// @Override
	// public boolean test(T o) {
	//
	// return true;
	// }
	// });
	// }

	/**
	 * Validate a input for rotation.
	 *
	 * @param input
	 *            user input
	 * @return true/false if its a valid degree
	 */
	@SuppressWarnings("finally")
	private boolean validateInputDegrees(String input) {
		boolean valid = false;
		try {
			Double value = Double.parseDouble(input);
			if (value <= 40.0 && value >= -40.0) {
				valid = true;
			}
		} catch (NumberFormatException e) {
			mainApp.getLogger().log(Level.WARNING, "The input it´s not a number", e);
		} finally {
			return valid;
		}
	}

	/**
	 * Validates whether all operations are correctly done or not.
	 *
	 * @return code List with errors or null if no errors: code: 1 - no units, 2
	 *         - no value, 3 - no line, 4 - Not cropped
	 */
	private LinkedList<Integer> validateOperations() {
		LinkedList<Integer> code = new LinkedList<>();
		Measure m = mainApp.getProject().getMeasure();
		if (m.getMeasureUnit() == null || m.getMeasureUnit().isEmpty()) {
			code.add(1);
		}
		if (m.getMeasureValue() == 0.0f) {
			code.add(2);
		}
		if (m.getStartMeasure() == null || m.getEndMeasure() == null) {
			code.add(3);
		}
		if (!isCropped) {
			code.add(4);
		}
		if (code.isEmpty()) {
			code = null;
		}
		return code;
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * Also, sets the Images. This is done here because when the method
	 * initialize is called, there is no reference to the mainapp.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		mainApp.getPrimaryStage().setMaximized(true);
		if (mainApp.getFullImage() != null) {
			if (mainApp.getCroppedImage() != null) {
				previewImage.setImage(mainApp.getCroppedImage());
				isCropped = true;
			} else {
				// Full image from the previous screen if there wasn't a cropped
				// image yet
				previewImage.setImage(mainApp.getFullImage());
			}

			originalImagePreview.setImage(mainApp.getFullImage());

			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			imgAux = SwingFXUtils.fromFXImage(originalImagePreview.getImage(), null);
			inputDegrees.setText("0.0");
			measure = mainApp.getProject().getMeasure();
		}
	}
}