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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import es.ubu.lsi.perikymata.MainApp;
import ij.io.Opener;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class ImageSelectionController {

	/**
	 * preview of the full tooth Image.
	 */
	@FXML
	private ImageView previewImage;

	/**
	 * List of selected images to stitch.
	 */
	@FXML
	private ListView<String> filesListView;

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	////////////////////// Status Elements////////////////////

	/**
	 * Current status, tells to the user if a Thread is running.
	 */
	@FXML
	private Label status;

	/**
	 * Loading gif.
	 */
	@FXML
	private ImageView loading;

	/////////////////////////////////////////////////////////////
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());

		// Loads loading gif.
		loading.setImage(new Image(this.getClass().getResource("/rsc/482.gif").toExternalForm()));
		loading.setVisible(false);
	}

	/**
	 * Opens a FileChooser to let the user select Image to load.
	 */
	@FXML
	private void handleOpen() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show open file dialog
		File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

		if (file != null) {

			java.awt.Image full = new Opener().openImage(file.getParent(), file.getName()).getImage();

			try (FileOutputStream fileStream = new FileOutputStream(
					new File(Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png").toString()))) {

				Files.copy(file.toPath(), fileStream);
			} catch (IOException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur opening full image.", e);
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Error opening full image");
				alert.setHeaderText("Can't open or copy full image.\n");
				alert.setContentText(
						"Can't open full image or copy it into the project folder.\n" + "file was: " + file.toString());
				alert.showAndWait();
			}

			mainApp.setFullImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
			previewImage.setImage(mainApp.getFullImage());
			mainApp.setFilteredImage(mainApp.getFullImage());

		}
	}

	/**
	 * Opens a FileChooser to let the user select multiple Images to load.
	 */
	@FXML
	private void handleOpenMultiple() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show open file dialog
		List<File> list = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());
		for (File file : list) {
			if (file != null) {
				mainApp.getFilesList().add(file.getName());

				try (FileOutputStream fileStream = new FileOutputStream(
						new File(Paths.get(mainApp.getProjectPath(), "Fragments").toString(), file.getName()))) {

					Files.copy(file.toPath(), fileStream);
				} catch (IOException e) {
					mainApp.getLogger().log(Level.SEVERE, "Exception occur opening fragment files.", e);
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Error opening or coping fragments.");
					alert.setHeaderText("Can't open or copy fragment file.\n");
					alert.setContentText("Cant open or copy the image to stitch with path:\n" + file.toString());
					alert.showAndWait();
				}

			}
		}
	}

	/**
	 * Handler that rotates image.
	 */
	@FXML
	private void handleRotate() {
		BufferedImage im = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
		AffineTransform a = new AffineTransform();
		a.translate(0.5 * im.getHeight(), 0.5 * im.getWidth());
		a.rotate(Math.PI / 2);
		a.translate(-0.5 * im.getWidth(), -0.5 * im.getHeight());
		AffineTransformOp op = new AffineTransformOp(a, AffineTransformOp.TYPE_BILINEAR);
		Image i = SwingFXUtils.toFXImage(op.filter(im, null), null);
		previewImage.setImage(i);
		mainApp.setFullImage(i);
		mainApp.setFilteredImage(i);

	}

	/**
	 * Handler that removes a image from the list.
	 */
	@FXML
	private void handleRemoveImage() {
		int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			Paths.get(mainApp.getProjectPath(), "Fragments", filesListView.getItems().get(selectedIndex)).toFile()
					.delete();
			filesListView.getItems().remove(selectedIndex);
		} else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("No Image Selected");
			alert.setContentText("Please select a image in the table to delete it");
			alert.showAndWait();
		}
	}

	/**
	 * Handler that changes to the image filter stage when called.
	 */
	@FXML
	private void nextScreen() {
		mainApp.showImageFilters();
	}

	/**
	 * Handler runs the OpenCV code to Stitch images together.
	 */
	@FXML
	private void launchStitcher() {

		changeStatus("Stitching, please wait");
		loading.setVisible(true);

		new Thread(() -> {
			try {
				mainApp.getRootLayout().setDisable(true);
				List<String> tempList = new ArrayList<>();
				StringBuilder tempString = new StringBuilder();
				mainApp.getFilesList()
						.forEach(x -> tempList.add(Paths.get(mainApp.getProjectPath(), "Fragments", x).toString()));
				for (String i : tempList) {
					tempString.append(" " + checkPath(i));
				}
				Process stitcher = Runtime.getRuntime().exec(getTempStitchingPath(tempString));
				int ok;
				// OK exit code is 1.
				if ((ok = stitcher.waitFor()) == 1) {

					// Copiar imagen a carpeta
					File tempFullImage = new File(System.getProperty("java.io.tmpdir") + "Full_Image.png");
					File finalFullImage = new File(
							Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png").toString());
					boolean copied = copyFile(tempFullImage, finalFullImage, true, false);
					if (copied) {
						java.awt.Image full = new Opener()
								.openImage(
										Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png").toString())
								.getImage();

						changeStatus("Stitching completed!");
						loading.setVisible(false);
						mainApp.getRootLayout().setDisable(false);
						Platform.runLater(() -> {
							mainApp.setFullImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
							this.previewImage.setImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
							mainApp.setFilteredImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
						});
					} else {
						changeStatus("Failed to load full image");
					}
				} else {
					changeStatus("Stitching failed.");
					loading.setVisible(false);
					mainApp.getLogger().log(Level.WARNING, "Stitching failed, exit with code: " + ok);

				}
			} catch (IOException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur executing stitcher.", e);

				changeStatus("Stitching failed.");
				loading.setVisible(false);
			} catch (InterruptedException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur waiting for stitching.", e);

				changeStatus("Thread interrupted.");
				loading.setVisible(false);
				Thread.currentThread().interrupt();
			} finally {
				loading.setVisible(false);
				mainApp.getRootLayout().setDisable(false);
			}
		}).start();
	}

	/**
	 * Check if a path has whitespace.
	 * 
	 * @param path
	 * @return path fixed with double quotes
	 */
	private String checkPath(String path) {
		String fragmentPath = path;
		if (path.contains(" ")) {
			if (System.getProperty("java.io.tmpdir").contains(" ")) {
				// TODO Preguntar por una carpeta temporal que no tenga espacios
			} else {
				String[] nameAndExtension = getImageNameAndExtension(path);
				try {
					File tempFragment = File.createTempFile(nameAndExtension[0], "." + nameAndExtension[1]);
					File staticFragment = new File(path);
					copyFile(staticFragment, tempFragment, false, true);
					fragmentPath = tempFragment.getAbsolutePath();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fragmentPath;
	}

	/**
	 * /** Copy a file. Delete source file if success.
	 * 
	 * @param sourceFile
	 *            source file
	 * @param targetFile
	 *            target file
	 * @param deleteSource
	 *            true if the source must be deleted when JVM ends.
	 * @param deleteTarget
	 *            true if the target must be deleted when JVM ends.
	 * @return true/false if success
	 */
	private boolean copyFile(File sourceFile, File targetFile, boolean deleteSource, boolean deleteTarget) {
		boolean copied = false;
		BufferedInputStream source;
		BufferedOutputStream target;
		try {
			source = new BufferedInputStream(new FileInputStream(sourceFile));
			target = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] bytes = new byte[2048];
			int i = source.read(bytes);
			while (i > 0) {
				target.write(bytes, 0, i);
				i = source.read(bytes);
			}
			source.close();
			target.close();
			if (deleteSource == true) {
				sourceFile.deleteOnExit();
			}
			if (deleteTarget == true) {
				targetFile.deleteOnExit();
			}
			targetFile.setReadable(true, false);
			copied = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copied;
	}

	/**
	 * Get the name and extension of the image.
	 * 
	 * @param path
	 *            of the image
	 * @return string array with the name and extension of the image
	 */
	private String[] getImageNameAndExtension(String path) {
		String[] splittedFileName = path.split(Pattern.quote(System.getProperty("file.separator")));
		String[] nameAndExtension = splittedFileName[splittedFileName.length - 1].split(Pattern.quote("."));
		return nameAndExtension;
	}

	/**
	 * Check if a folder can be used as temporary folder
	 * 
	 * @param path
	 *            of the folder to be checked
	 * @return true/false
	 */
	private boolean checkTempFolder(String path) {
		// TODO Comprobar que no hay espacios Comprobar que se puede leer y
		// escribir
		return true;
	}

	/**
	 * Create the command which will be execute.
	 * 
	 * @param tempString
	 *            string with the path of the different image fragments
	 * @return string to be execute
	 */
	private String getTempStitchingPath(StringBuilder tempString) {
		// AMT 07/02/2017 Select Stitching executable from resources depending
		// Host OS
		String resourcePath = "rsc/stitching/bin/";
		File stitchingTemp;
		String tempStitchingPath = "";
		try {
			if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
				resourcePath += "Stitching32.exe";
				stitchingTemp = File.createTempFile("Stitching32", ".exe");
			} else {
				if (System.getProperty("os.arch").contains("64")) {
					resourcePath += "Stitching64Dinamic.ubu";
					stitchingTemp = File.createTempFile("Stitching64Dinamic", ".ubu");
				} else {
					resourcePath += "Stitching32Dinamic.ubu";
					stitchingTemp = File.createTempFile("Stitching32Dinamic", ".ubu");
				}
			}

			File stitchingResource = new File(resourcePath);
			copyFile(stitchingResource, stitchingTemp, false, true);
			stitchingTemp.setExecutable(true, false);
			// TODO Comprobar la carpeta temporal por defecto
			tempStitchingPath = stitchingTemp.toString() + " " + System.getProperty("java.io.tmpdir") + "Full_Image.png"
					+ " " + tempString;
			return tempStitchingPath;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempStitchingPath;
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
		filesListView.setItems(mainApp.getFilesList());
	}

	/**
	 * Changes the text of the status label from the Platform because label
	 * can't be changed directly from a thread.
	 * 
	 * @param text
	 */
	private synchronized void changeStatus(String text) {
		Platform.runLater(() -> status.setText(text));
	}
}
