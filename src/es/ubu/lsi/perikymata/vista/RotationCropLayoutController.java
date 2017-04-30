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

import es.ubu.lsi.perikymata.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());
    }

    @FXML
    void previousScreen(ActionEvent event) {

    }

    @FXML
    void handleRotation(ActionEvent event) {

    }

    @FXML
    void handleCrop(ActionEvent event) {

    }

    @FXML
    void nextScreen(ActionEvent event) {

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
		}
	}
}