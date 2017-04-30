package es.ubu.lsi.perikymata.vista;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;

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

import es.ubu.lsi.perikymata.MainApp;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

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
	 * Rotation radians for the image.
	 */
	private Double rotationRadians;

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());
		rotationRadians = 0.0;
	}

	@FXML
	void previousScreen() {
		try {
			System.out.println("Previous Screen");
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception loading previous stage.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error loading previous stage.");
			alert.setHeaderText("Can't load previous stage.\n");
			alert.setContentText("Can't load previous stage");
			alert.showAndWait();
		}

	}

	@FXML
	void handleRotation() {
		try {
			System.out.println(rotationSlider.getValue());
			rotationRadians = Math.toRadians(rotationSlider.getValue());
			AffineTransform transform = new AffineTransform();
			transform.rotate(rotationRadians, img.getWidth() / 2, img.getHeight() / 2);
			AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			Image i = SwingFXUtils.toFXImage(op.filter(img, null), null);
			previewImage.setImage(i);
			mainApp.setFullImage(i);
			mainApp.setFilteredImage(i);
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur rotating image.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error rotating image");
			alert.setHeaderText("Can't rotate image.\n");
			alert.setContentText("Can't rotate image");
			alert.showAndWait();
		}

	}

	@FXML
	void handleCrop() {
		try {
			System.out.println("Crop");
		} catch (Exception e) {
			mainApp.getLogger().log(Level.SEVERE, "Exception occur cropping image.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error cropping image");
			alert.setHeaderText("Can't crop image.\n");
			alert.setContentText("Can't crop image");
			alert.showAndWait();
		}

	}

	@FXML
	void nextScreen() {
		try {
			System.out.println("Next Screen");
			System.out.println("saving at: " + mainApp.getProjectPath()+File.separator);
			File outputfile = new File(mainApp.getProjectPath() + File.separator + "TEST.jpg");
			ImageIO.write(SwingFXUtils.fromFXImage(mainApp.getFilteredImage(),null), "png", outputfile);
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
		if (mainApp.getFullImage() != null) {
			previewImage.setImage(mainApp.getFullImage());
			// Full image from the previous screen
			img = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
		}
	}
}