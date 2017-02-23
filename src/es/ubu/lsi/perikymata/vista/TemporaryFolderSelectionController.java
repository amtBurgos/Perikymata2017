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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import es.ubu.lsi.perikymata.util.StitchingUtil;

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
	 * Label as error when the default system temporary folder has whitespaces.
	 */
	@FXML
	private Label lbErrorDefaultTempFolder;

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
	 * Label as error when the selected folder its not valid.
	 */
	@FXML
	private Label lbErrorCustomFolder;

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
	private StitchingUtil tempUtil;

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		tempUtil = new StitchingUtil();
		lbErrorCustomFolder.setVisible(false);
		lbErrorDefaultTempFolder.setVisible(false);
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
					if (!tempUtil.isTempFolderValid(defaultTempFolder)) {
						cb.setSelected(false);
						cb.setDisable(true);
						lbErrorDefaultTempFolder.setVisible(true);
						btnChooseFolder.setDisable(false);
						tfSelectedPath.setDisable(false);
						tfSelectedPath.setEditable(true);
						tempFolder = Folder.DEFAULT;
					} else {
						// If default is valid
						tempFolder = Folder.DEFAULT;
						lbErrorDefaultTempFolder.setVisible(false);
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
		// FileChooser fileChooser = new FileChooser();

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder without whitespace and enough permissions");
		File selectedFolder = directoryChooser.showDialog(mainApp.getPrimaryStage());
		if (tempUtil.isTempFolderValid(selectedFolder.toString())) {
			lbErrorCustomFolder.setVisible(false);
			tempFolder = Folder.CUSTOM;
			tfSelectedPath.setText(selectedFolder.toString());

		} else {
			lbErrorCustomFolder.setVisible(true);
		}
	}

	/**
	 * Save the selected folder as a the temporary folder for the proyect.
	 */
	@FXML
	private void save() {
		if (tempFolder == Folder.DEFAULT) {
			if (tempUtil.isTempFolderValid(defaultTempFolder)) {
				mainApp.getProject().setTemporaryFolder("DEFAULT");
			} else {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Invalid path");
				alert.setHeaderText("The default system temporary path is invalid.\n");
				alert.setContentText(
						"Please, select a custom location without whitespaces and with enough permissions.\n");
				alert.showAndWait();
			}
		} else if (tempFolder == Folder.CUSTOM) {
			if (tempUtil.isTempFolderValid(tfSelectedPath.getText())) {
				mainApp.getProject().setTemporaryFolder(tfSelectedPath.getText());
			} else {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Invalid path");
				alert.setHeaderText("Invalid custom temporary path.\n");
				alert.setContentText(
						"Please, select a custom location without whitespaces and with enough permissions.\n");
				alert.showAndWait();
			}
		}
		mainApp.makeProjectXml();
		Stage stage = (Stage) btnSave.getScene().getWindow();
		stage.close();
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
	 * is executed mainapp it's not set yet.
	 */
	public void initializeComponents() {
		xmlTempFolder = mainApp.getProject().getTemporaryFolder();
		if (xmlTempFolder.toUpperCase().equals("DEFAULT")) {
			if (tempUtil.isTempFolderValid(defaultTempFolder)) {
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
				lbErrorDefaultTempFolder.setVisible(true);
				tfSelectedPath.setText("");
				tfSelectedPath.setEditable(true);
			}
		} else {
			// If there is not the default folder in XML
			if (!tempUtil.isTempFolderValid(defaultTempFolder)) {
				tempFolder = Folder.CUSTOM;
				cbTempFolder.setSelected(false);
				cbTempFolder.setDisable(true);
				lbErrorDefaultTempFolder.setVisible(true);
				tfSelectedPath.setText(xmlTempFolder);
				tfSelectedPath.setEditable(true);
			} else {
				tempFolder = Folder.CUSTOM;
				cbTempFolder.setSelected(false);
				cbTempFolder.setDisable(false);
				lbErrorDefaultTempFolder.setVisible(false);
				tfSelectedPath.setText(xmlTempFolder);
				tfSelectedPath.setEditable(true);
			}
		}
		checkBoxBehaviour(cbTempFolder);
	}

}
