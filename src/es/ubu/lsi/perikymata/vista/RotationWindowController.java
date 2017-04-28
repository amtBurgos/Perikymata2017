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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * View for image rotation.
 *
 * @author Andres Miguel Teran
 *
 */
public class RotationWindowController {

	/**
	 * Container for the image.
	 */
	@FXML // fx:id="imagePane"
	private Pane imagePane; // Value injected by FXMLLoader

	/**
	 * Back button.
	 */
	@FXML // fx:id="backBtn"
	private Button backBtn; // Value injected by FXMLLoader

	/**
	 * Continue button.
	 */
	@FXML // fx:id="continueBtn"
	private Button continueBtn; // Value injected by FXMLLoader

	/**
	 * Apply button.
	 */
	@FXML // fx:id="applyBtn"
	private Button applyBtn; // Value injected by FXMLLoader

	/**
	 * Text field.
	 */
	@FXML // fx:id="textField"
	private TextField textField; // Value injected by FXMLLoader

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * preview of the full tooth Image.
	 */
	@FXML
	private ImageView previewImage;

	/**
	 * Initializes components of the window.
	 */
	@FXML
	void initialize() {
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());
	}

	/**
	 * Goes to the previous screen.
	 *
	 * @param event
	 *            event
	 */
	@FXML
	private void previousScreen(ActionEvent event) {
		mainApp.showImageSelection();
	}

	/**
	 * Apply the inserted rotation.
	 *
	 * @param event
	 *            event
	 */
	@FXML
	private void applyRotation(ActionEvent event) {

	}

	/**
	 * Goes to the next screen.
	 *
	 * @param event
	 *            event
	 */
	@FXML
	private void nextScreen(ActionEvent event) {
		mainApp.showImageFilters();
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

		// Add observable list data to the table
		//filesListView.setItems(mainApp.getFilesList());
	}
}
