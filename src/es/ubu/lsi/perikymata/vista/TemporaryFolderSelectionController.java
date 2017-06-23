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
import java.io.File;
import java.util.logging.Level;

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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import es.ubu.lsi.perikymata.util.StitchingTemporaryUtil;

/**
 * Controller for the select temporary folder window. Contains method for
 * selecting a custom temporary location for stitching the images and for
 * checking if that path doesn't contains whitespaces.
 *
 * @author Andres Miguel Teran
 *
 */
public class TemporaryFolderSelectionController {

	/**
	 * The folder which will be saved as temporary folder
	 */
	private enum Folder {
		/**
		 * Default system temporary folder
		 */
		DEFAULT,
		/**
		 * Custom temporary folder
		 */
		CUSTOM
	}

	/**
	 * Checkbox for using the default system temporary folder.
	 */
	@FXML
	private CheckBox cbTempFolder;

	/**
	 * Button for choosing a folder.
	 */
	@FXML
	private Button btnChooseFolder;

	/**
	 * Path select to be the temporary folder.
	 */
	@FXML
	private TextField tfSelectedPath;

	/**
	 * Cancel Button.
	 */
	@FXML
	private Button btnCancel;

	/**
	 * Set and Save Button.
	 */
	@FXML
	private Button btnSave;

	/**
	 * Type of Temporary folder.
	 */
	private Folder tempFolder;

	/**
	 * Path to default system temporary folder;
	 */
	private String defaultTempFolder;

	/**
	 * Temporary folder path from XML file;
	 */
	private String xmlTempFolder;

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	/**
	 * Util for validate temporary paths.
	 */
	private StitchingTemporaryUtil stitchingUtil;

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		stitchingUtil = new StitchingTemporaryUtil();
		cbTempFolder.setAllowIndeterminate(false);
		defaultTempFolder = System.getProperty("java.io.tmpdir");
	}

	/**
	 * Handle the change event on checkbox.
	 *
	 * @param cb
	 *            checkbox
	 */
	private void checkBoxBehaviour(CheckBox cb) {
		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					// Check if system temporary is valid
					if (!stitchingUtil.isTempFolderValid(defaultTempFolder)) {
						cb.setSelected(false);
						cb.setDisable(true);
						showErrorDefautlTempFolder();
						btnChooseFolder.setDisable(false);
						tfSelectedPath.setDisable(false);
						tfSelectedPath.setEditable(true);
						tempFolder = Folder.DEFAULT;
					} else {
						// If default is valid
						tempFolder = Folder.DEFAULT;
						btnChooseFolder.setDisable(true);
						tfSelectedPath.setDisable(true);
					}
				} else {
					tempFolder = Folder.CUSTOM;
					cb.setDisable(false);
					btnChooseFolder.setDisable(false);
					tfSelectedPath.setDisable(false);
					tfSelectedPath.setEditable(true);
				}
			}
		});
	}

	/**
	 * Opens a FileChooser to select a custom temporary folder.
	 */
	@FXML
	private void chooseFolder() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder without whitespace and enough permissions");
		File selectedFolder = directoryChooser.showDialog(mainApp.getPrimaryStage());
		if (stitchingUtil.isTempFolderValid(selectedFolder.toString())) {
			tempFolder = Folder.CUSTOM;
			tfSelectedPath.setText(selectedFolder.toString());

		} else {
			showErrorCustomTempFolder();
		}
	}

	/**
	 * Save the selected folder as a the temporary folder for the proyect.
	 */
	@FXML
	private void save() {
		boolean save = false;
		if (tempFolder == Folder.DEFAULT) {
			if (stitchingUtil.isTempFolderValid(defaultTempFolder)) {
				mainApp.getProject().setTemporaryFolder("DEFAULT");
				save = true;
			} else {
				showErrorDefautlTempFolder();
			}
		} else if (tempFolder == Folder.CUSTOM) {
			if (stitchingUtil.isTempFolderValid(tfSelectedPath.getText())) {
				String tmpFolder = tfSelectedPath.getText();
				if (!tmpFolder.substring(tmpFolder.length() - 1).equals(System.getProperty("file.separator"))) {
					tmpFolder += File.separator;
				}
				mainApp.getProject().setTemporaryFolder(tmpFolder);
				save = true;
			} else {
				showErrorCustomTempFolder();
			}
		}
		if (save) {
			mainApp.makeProjectXml();
			Stage stage = (Stage) btnSave.getScene().getWindow();
			stage.close();
		}
	}

	/**
	 * Close the window without save;
	 */
	@FXML
	private void cancel() {
		Stage stage = (Stage) btnCancel.getScene().getWindow();
		stage.close();
	}

	public void disableCancel(boolean disable) {
		if (disable) {
			btnCancel.setVisible(false);
			Platform.setImplicitExit(false);
			mainApp.getPrimaryStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					event.consume();
				}
			});
		}
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * This is done here because when the method initialize is called, there is
	 * no reference to the mainapp.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	/**
	 * Initialize some components. It is necessary because when the initialize()
	 * is executed if mainapp it's not set yet.
	 */
	public void initializeComponents() {
		xmlTempFolder = mainApp.getProject().getTemporaryFolder();
		if (xmlTempFolder.toUpperCase().equals("DEFAULT")) {
			if (stitchingUtil.isTempFolderValid(defaultTempFolder)) {
				tempFolder = Folder.DEFAULT;
				cbTempFolder.setSelected(true);
				cbTempFolder.setDisable(false);
				btnChooseFolder.setDisable(true);
				tfSelectedPath.setEditable(false);
			} else {
				// If default folder is invalid
				tempFolder = Folder.CUSTOM;
				cbTempFolder.setSelected(false);
				cbTempFolder.setDisable(true);
				showErrorDefautlTempFolder();
				tfSelectedPath.setText("");
				tfSelectedPath.setEditable(true);
			}
		} else {
			// If there is not the default folder in XML
			if (!stitchingUtil.isTempFolderValid(defaultTempFolder)) {
				tempFolder = Folder.CUSTOM;
				cbTempFolder.setSelected(false);
				cbTempFolder.setDisable(true);
				showErrorCustomTempFolder();
				tfSelectedPath.setText(xmlTempFolder);
				tfSelectedPath.setEditable(true);
			} else {
				tempFolder = Folder.CUSTOM;
				cbTempFolder.setSelected(false);
				cbTempFolder.setDisable(false);
				tfSelectedPath.setText(xmlTempFolder);
				tfSelectedPath.setEditable(true);
			}
		}
		checkBoxBehaviour(cbTempFolder);
	}

	/**
	 * Shows an alert windows explaining why the default temporary folder option
	 * can not be marked.
	 */
	private void showErrorDefautlTempFolder() {
		mainApp.getLogger().log(Level.INFO, "Default temporary folder can't be saved because is invalid.");
		Alert alert = new Alert(Alert.AlertType.ERROR);
		Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
		alert.setTitle("Default temporary folder invalid");
		alert.setHeaderText("The default system temporary path is invalid.\n");
		alert.setContentText("Please, select a custom location without whitespaces and with enough permissions.\n");
		alert.showAndWait();
	}

	/**
	 * Shows an alert windows explaining why the custom temporary folder option
	 * can not be selected.
	 */
	private void showErrorCustomTempFolder() {
		mainApp.getLogger().log(Level.INFO, "Custom temporary folder can't be saved because is invalid.");
		Alert alert = new Alert(Alert.AlertType.ERROR);
		Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
		alert.setTitle("Custom temporary folder invalid");
		alert.setHeaderText("The custom temporary path selected is invalid.\n");
		alert.setContentText("Please, select a location without whitespaces and with enough permissions.\n");
		alert.showAndWait();
	}

}
