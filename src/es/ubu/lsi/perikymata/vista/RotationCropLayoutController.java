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
import java.util.ArrayList;
import java.util.List;
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
	 * Rotation in radians for the image.
	 */
	private Double rotationRadians;

	/**
	 * True if the crop button is selected.
	 */
	private boolean cropping;

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
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		areaSelectorBtnImage.setImage(new Image(this.getClass().getResource("/rsc/Square-icon.png").toExternalForm()));
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());
		rotationRadians = 0.0;
		cropping = false;
		croppingPoints = new Point2D.Double[2];
		pane = (Pane) previewImage.getParent();
		rect = null;
		initMouseEventHandler();
	}

	/**
	 * Initializes the mouse event handler.
	 */
	private void initMouseEventHandler() {
		mousePressedHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (cropping == false) {
					// First click
					System.out.println("Primer click");
					croppingPoints[0] = new Point2D.Double(event.getX(), event.getY());

					// Discard previous rectangles in the pane
					pane.getChildren().removeIf(new Predicate<Object>() {
						@Override
						public boolean test(Object o) {
							return o instanceof Rectangle;
						}
					});

					// Change flag
					cropping = true;
				} else {
					// Second click
					System.out.println("Segundo click");
					croppingPoints[1] = new Point2D.Double(event.getX(), event.getY());

					rect = new Rectangle(croppingPoints[0].x, croppingPoints[0].y,
							Math.abs(croppingPoints[1].x - croppingPoints[0].x),
							Math.abs(croppingPoints[1].y - croppingPoints[0].y));
					rect.setStroke(Color.BLUE);
					rect.setStrokeWidth(0.8);
					rect.setStrokeLineCap(StrokeLineCap.SQUARE);
					rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

					// Bind rectangle to parent container (Pane)
					// rect.heightProperty().bind(pane.heightProperty().subtract(20));
					// rect.widthProperty().bind(pane.widthProperty().subtract(20));

					// rect.heightProperty().bind(previewImage.fitHeightProperty());
					// rect.widthProperty().bind(previewImage.fitWidthProperty());

					pane.getChildren().add(rect);

					// Change flag
					cropping = false;
				}
			}
		};
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
	 * Handles the rotation feature.
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
			// mainApp.setFullImage(i);
			// mainApp.setFilteredImage(i);
			inputDegrees.setText(String.valueOf(Math.floor(rotationSlider.getValue())));
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
					// mainApp.setFullImage(i);
					// mainApp.setFilteredImage(i);
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
				alert.setContentText("Please, insert a number between -180.0 and 180.0.");
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
			if (value <= 180.0 && value >= -180.0) {
				valid = true;
			}
		} catch (NumberFormatException e) {
			mainApp.getLogger().log(Level.WARNING, "The input it´s not a number", e);
		} finally {
			return valid;
		}
	}

	/**
	 * Handle the crop feature.
	 */
	@FXML
	private void handleCrop() {
		try {
			System.out.println("Cropping");

			// If the user has selected an area to crop
			if (rect != null) {

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
	 * Handles the opening of the area selector.
	 */
	@FXML
	private void handleSelectorArea() {
		try {
			if (areaSelectorBtn.isSelected()) {
				previewImage.setCursor(Cursor.CROSSHAIR);
				System.out.println("Area Selector selected");
				// Add listener to start a rectangle on the image
				previewImage.addEventHandler(MouseEvent.MOUSE_CLICKED, mousePressedHandler);
			} else {
				System.out.println("Area Selector deselected");
				previewImage.setCursor(Cursor.DEFAULT);
				// Remove listener to start a rectangle on the image
				previewImage.removeEventHandler(MouseEvent.MOUSE_CLICKED, mousePressedHandler);
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
	 * Goes to the next stage, perikymata counter.
	 */
	@FXML
	private void nextScreen() {
		try {
			System.out.println("Next Screen");
			System.out.println("saving at: " + mainApp.getProjectPath() + File.separator);
			File outputfile = new File(mainApp.getProjectPath() + File.separator + "TEST.jpg");
			ImageIO.write(SwingFXUtils.fromFXImage(mainApp.getFilteredImage(), null), "png", outputfile);
			// TODO Aplicar cambios y guardar
			// mainApp.setFullImage(previewImage.getImage());
			// mainApp.setFilteredImage(previewImage.getImage());
			// mainApp.showPerikymataCount();
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
		mainApp.getPrimaryStage().setFullScreen(true);
		if (mainApp.getFullImage() != null) {
			previewImage.setImage(mainApp.getFullImage());
			// Full image from the previous screen
			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
			inputDegrees.setText("0.0");
		}
	}
}