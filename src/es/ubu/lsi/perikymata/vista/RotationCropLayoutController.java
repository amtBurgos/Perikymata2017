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
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import es.ubu.lsi.perikymata.MainApp;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.event.EventHandler;
import java.awt.geom.Point2D;

/**
 * Control for the rotation and crop view.
 *
 * @author Andres Miguel Teran
 *
 */
public class RotationCropLayoutController {

	/**
	 * Save and continue button.
	 */
	@FXML // fx:id="saveAndContinueBtn"
	private Button saveAndContinueBtn; // Value injected by FXMLLoader

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
	 * Slider for rotation.
	 */
	@FXML // fx:id="rotationSlider"
	private Slider rotationSlider; // Value injected by FXMLLoader

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
	 * Preview image.
	 */
	@FXML // fx:id="previewImage"
	private ImageView previewImage; // Value injected by FXMLLoader

	/**
	 * Input for the degrees rotation.
	 */
	@FXML // fx:id="inputDegrees"
	private TextField inputDegrees; // Value injected by FXMLLoader

	/**
	 * Return button.
	 */
	@FXML // fx:id="returnBtn"
	private Button returnBtn; // Value injected by FXMLLoader

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * Full image.
	 */
	private BufferedImage img;

	/**
	 * Auxiliar image, used to reset the previewImage when reset button is
	 * pressed.
	 */
	private BufferedImage imgAux;

	/**
	 * Rotation in radians for the image.
	 */
	private Double rotationRadians;

	/**
	 * List with the points to create a rectangle.
	 */
	private Point2D.Double[] croppingPoints;

	/**
	 * Rectangle for crop.
	 */
	private Rectangle rect;

	/**
	 * Pane for the previewImage.
	 */
	private Pane pane;

	/**
	 * Customs mouse event handler.
	 */
	private EventHandler<MouseEvent> mousePressedHandler, mouseDraggedHandler, mouseReleasedHandler;

	/**
	 * image cropped.
	 */
	private WritableImage imageCropped;

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
		rotationRadians = 0.0;
		croppingPoints = new Point2D.Double[2];
		pane = (Pane) previewImage.getParent();
		pane.setStyle("-fx-background-color: #000000;");
		rect = null;
		imageCropped = null;
		initMouseEventHandler();
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

	/**
	 * Goes to the previous stage, image selection and stitching.
	 */
	@FXML
	private void previousScreen() {
		try {
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
	 * Handle the crop feature, load the image cropped and save a copy to disk.
	 */
	@FXML
	private void handleCrop() {
		try {
			// If the user has selected an area to crop
			if (rect != null) {
				// Only needed X or Y because of the 'preservate ratio' check-in
				// in the fxml file
				Double ratio = previewImage.getBoundsInParent().getWidth() / previewImage.getImage().getWidth();

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
	 * Resets the view of the image to zero degrees rotation and original image.
	 */
	@FXML
	private void handleReset() {
		try {
			removeRectanglesFromView();
			previewImage.setImage(SwingFXUtils.toFXImage(imgAux, null));
			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			rotationSlider.setValue(0.0);
			inputDegrees.setText("0.0");
			if (areaSelectorBtn.isSelected()) {
				previewImage.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
				previewImage.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
				previewImage.setCursor(Cursor.DEFAULT);
				areaSelectorBtn.setSelected(false);
			}
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur resetting view", e);
		}
	}

	/**
	 * Goes to the next stage, perikymata counter.
	 */
	@FXML
	private void nextScreen() {
		try {
			if (imageCropped != null) {
				// Save cropped image to disk, and load it in the image preview
				BufferedImage bfImage = SwingFXUtils.fromFXImage(imageCropped, null);
				File outputfile = new File(mainApp.getProjectPath() + File.separator + "Cropped_Image.png");
				outputfile.setWritable(true, false);
				ImageIO.write(bfImage, "png", outputfile);
			}
			mainApp.setFullImage(previewImage.getImage());
			mainApp.setFilteredImage(previewImage.getImage());
			mainApp.showPerikymataCount();
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
			previewImage.setImage(mainApp.getFullImage());
			// Full image from the previous screen
			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			imgAux = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			inputDegrees.setText("0.0");
		}
	}
}