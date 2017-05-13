package es.ubu.lsi.perikymata;

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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.util.StitchingUtil;
import es.ubu.lsi.perikymata.modelo.Project;
import es.ubu.lsi.perikymata.modelo.filters.Filter;
import es.ubu.lsi.perikymata.vista.ImageFiltersController;
import es.ubu.lsi.perikymata.vista.ImageSelectionController;
import es.ubu.lsi.perikymata.vista.PerikymataCountController;
import es.ubu.lsi.perikymata.vista.RootLayoutController;
import es.ubu.lsi.perikymata.vista.RotationCropLayoutController;
import es.ubu.lsi.perikymata.vista.TemporaryFolderSelectionController;
import ij.io.Opener;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the main application, contains the data that needs to be
 * accessed by any of the other windows and has common operations, like
 * navigation between windows or data access.
 *
 * @author Sergio Chico Carrancio
 * @author Andres Miguel Teran
 */
public class MainApp extends Application {

	/**
	 * Main container of the application's layout.
	 */
	private Stage primaryStage;
	/**
	 * Main layout of the application.
	 */
	private BorderPane rootLayout;

	/**
	 * @return the rootLayout
	 */
	public BorderPane getRootLayout() {
		return rootLayout;
	}

	/**
	 * @param rootLayout
	 *            the rootLayout to set
	 */
	public void setRootLayout(BorderPane rootLayout) {
		this.rootLayout = rootLayout;
	}

	/**
	 * File logger.
	 */
	private Logger logger = Logger.getLogger(MainApp.class.getName());

	/**
	 * Full image of a tooth, used to count perikyma.
	 */
	private Image fullImage;

	/**
	 * Full image of a tooth with some applied filters.
	 */
	private Image filteredImage;

	/**
	 * Cropped image with the dental crown.
	 */
	private Image croppedImage;

	/**
	 * List of files to stitch.
	 */
	private ObservableList<String> filesList = FXCollections.observableArrayList();

	/**
	 * List of applied filters.
	 */
	private ObservableList<Filter> appliedFilters = FXCollections.observableArrayList();

	/**
	 * Data of a perikymata project.
	 */
	private Project project;

	/**
	 * Opened project Path
	 */
	private String projectPath;

	/**
	 * Util for temporary folder validation.
	 */
	private StitchingUtil tempUtil = new StitchingUtil();

	/**
	 * Launches the applications, no args needed.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		configureLogger();
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Perikymata - Unsaved Project");
		this.primaryStage.getIcons()
				.add(new Image(this.getClass().getResource("/rsc/Tooth-icon.png").toExternalForm()));
		this.primaryStage.setMinHeight(450.0);
		this.primaryStage.setMinWidth(650.0);
		initRootLayout();
		showImageSelection();

	}

	/**
	 * Configures the logger to log in a file.
	 */
	public void configureLogger() {
		try {
			getLogger().setLevel(Level.ALL);
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			FileHandler fileHandler = new FileHandler("errorLog_" + dateFormat.format(date) + ".log");
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(new SimpleFormatter());
			getLogger().addHandler(fileHandler);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Exception creating logging file.", e);
		}
	}

	/**
	 * Loads and shows the RootLayout and tries to load the last opened file.
	 */
	public void initRootLayout() {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Puts the rootlayout(menubar) into the scene.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			// Gives a mainapp's reference to the controller.
			RootLayoutController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.show();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading the root layout.", e);
		}

		// Tries to load the last opened file.
		File file = getProjectFilePathProperty();
		if (file != null) {
			// if the file exists, it is loaded. If it doesn't the reference is
			// erased.
			loadProjectFromFile(file);
		} else {
			Boolean cont = false;
			while (!cont) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("A project needs to be open.");
				alert.setHeaderText("You need to open or create a new project to continue.");
				alert.setContentText("Cancel will close the application.");

				ButtonType buttonTypeNew = new ButtonType("New Project");
				ButtonType buttonTypeOpen = new ButtonType("Open Project");
				ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

				alert.getButtonTypes().setAll(buttonTypeNew, buttonTypeOpen, buttonTypeCancel);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == buttonTypeNew) {
					cont = createProject();
				} else if (result.get() == buttonTypeOpen) {
					cont = openProject();
				} else {
					System.exit(0);
				}
			}
		}

	}

	/**
	 * Creates or updates the current project XML.
	 */
	public void makeProjectXml() {
		File parent = new File(this.getProjectPath());

		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Project.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// Marshalling and saving XML to the file.
			File projectXMLfile = new File(parent.toString() + File.separator + parent.getName() + ".xml");
			m.marshal(getProject(), projectXMLfile);

			// Save the file path to the registry.
			setProjectFilePathProperty(projectXMLfile);
		} catch (JAXBException e) {

			this.getLogger().log(Level.SEVERE, "Exception occur creating XML.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error saving project");
			alert.setHeaderText("Project was not saved.\n");
			alert.setContentText("Error saving project on path: " + parent.toString());
			alert.showAndWait();
		}
	}

	/**
	 * Loads a Project XML file into the application.
	 *
	 * @param file
	 *            XML project.
	 */
	public void loadProjectFromFile(File file) {
		try {
			JAXBContext context = JAXBContext.newInstance(Project.class);
			Unmarshaller um = context.createUnmarshaller();

			// reads the XML and saves its data into a Project class.
			project = (Project) um.unmarshal(file);
			this.primaryStage.setTitle("Perikymata - " + project.getProjectName());

			// Adds the filters from the xml to the list of filters.
			if (project.getFilterList() != null) {
				this.getAppliedFilters().addAll(project.getFilterList());
			}

			// Adds the Full image to the project (if exists)
			//File fullImageFile = Paths.get(file.getParent(), "Full_image", "Full_image.png").toFile();
			File fullImageFile = Paths.get(file.getParent(), "Full_Image", "Full_image.png").toFile();
			if (fullImageFile.exists()) {
				java.awt.Image full = new Opener().openImage(fullImageFile.getPath()).getImage();
				setFullImage(SwingFXUtils.toFXImage((BufferedImage) full, null));

				// Adds the filtered image to the project (if exists)
				//File filteredImageFile = Paths.get(file.getParent(), "Full_image", "Filtered_image.png").toFile();
				File filteredImageFile = Paths.get(file.getParent(), "Full_Image", "Filtered_image.png").toFile();
				if (filteredImageFile.exists()) {
					java.awt.Image filtered = new Opener().openImage(filteredImageFile.getAbsolutePath()).getImage();
					setFilteredImage(SwingFXUtils.toFXImage((BufferedImage) filtered, null));
				} else {
					setFilteredImage(getFullImage());
				}

				// Adds the cropped image to the project (if exists)
				File croppedImageFile = Paths.get(file.getParent(), "Cropped_Image", "Cropped_image.png").toFile();
				if (croppedImageFile.exists()) {
					java.awt.Image cropped = new Opener().openImage(croppedImageFile.getAbsolutePath()).getImage();
					setCroppedImage(SwingFXUtils.toFXImage((BufferedImage) cropped, null));
				} else {
					// If doesn't exists we set it to null
					setCroppedImage(null);
				}
			}

			// Adds the names of the files under the folder "fragments" to the
			// list of
			// images to stitch.
			File fragmentsFolder = Paths.get(file.getParent(), "Fragments").toFile();
			for (File fragments : fragmentsFolder.listFiles()) {
				if (fragments != null) {
					getFilesList().add(fragments.getName());
				}
			}

			// Saves the path of the opened file.
			setProjectFilePathProperty(file);
			setProjectPath(file.getParent());

		} catch (JAXBException e) {

			this.getLogger().log(Level.SEVERE, "Exception occur loading project.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error loading project");
			alert.setHeaderText("Project cannot be loaded.\n");
			alert.setContentText("Error loading project on path: " + file.toString());
			alert.showAndWait();
			this.clearData();
		}
	}

	/**
	 * Shows the Image Selection Window.
	 */
	public void showImageSelection() {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/ImageSelection.fxml"));
			AnchorPane imageSelection = (AnchorPane) loader.load();

			// Shows this layout in the center of the rootLayout.
			rootLayout.setCenter(imageSelection);

			// Gives a mainapp's reference to the controller of the layout.
			ImageSelectionController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading imageSelection Stage.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Internal error.");
			alert.setHeaderText("Error loading image selection stage.\n");
			alert.setContentText("This application will close now, please try again.\n");
			alert.showAndWait();
			System.exit(-1);
		}
	}

	/**
	 * Shows the Filter Application Window.
	 */
	public void showImageFilters() {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/ImageFilters.fxml"));
			AnchorPane imageFilters = (AnchorPane) loader.load();

			// Shows this layout in the center of the rootLayout.
			rootLayout.setCenter(imageFilters);

			// Gives a mainapp's reference to the controller of the layout.
			ImageFiltersController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading imageFilters Stage.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Internal error.");
			alert.setHeaderText("Error loading image filtering stage.\n");
			alert.setContentText("This application will close now, please try again.\n");
			alert.showAndWait();
			System.exit(-1);
		}
	}

	/**
	 * Shows the Rotation Window.
	 */
	public void showRotationCrop() {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/RotationCropLayout.fxml"));
			BorderPane window = (BorderPane) loader.load();

			// Shows this layout in the center of the rootLayout.
			rootLayout.setCenter(window);

			// Gives a mainapp's reference to the controller of the layout.
			RotationCropLayoutController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading rotation and crop Stage.", e);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Internal error.");
			alert.setHeaderText("Error loading rotation stage.\n");
			alert.setContentText("This application will close now, please try again.\n");
			alert.showAndWait();
			System.exit(-1);
		}
	}

	/**
	 * Shows the Perikymata counting window.
	 */
	public void showPerikymataCount() {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/PerikymataCount.fxml"));
			AnchorPane perikymataCount = (AnchorPane) loader.load();

			// Shows this layout in the center of the rootLayout.
			rootLayout.setCenter(perikymataCount);

			// Gives a mainapp's reference to the controller of the layout.
			PerikymataCountController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading PerikymataCount Stage.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Internal error.");
			alert.setHeaderText("Error loading perikymata counting stage.\n");
			alert.setContentText("This application will close now, please try again.\n");
			alert.showAndWait();
			System.exit(-1);
		}
	}

	/**
	 * Shows the Temporary folder selection Window.
	 */
	public void showTemporaryFolderSelection(boolean showCancel) {
		try {
			// Loads the FXML view.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("vista/TemporaryFolderSelection.fxml"));
			Parent parent = (Parent) loader.load();
			Stage window = new Stage();
			window.setScene(new Scene(parent));
			window.setTitle("Temporary Folder Selection");
			window.show();

			// Gives a mainapp's reference to the controller of the layout.
			TemporaryFolderSelectionController controller = loader.getController();
			controller.setMainApp(this);
			controller.disableCancel(showCancel);
			controller.initializeComponents();

		} catch (Exception e) {
			this.getLogger().log(Level.SEVERE, "Exception occur loading temporary folder selection window.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Internal error.");
			alert.setHeaderText("Error loading temporary folder selection window.\n");
			alert.setContentText("This application will close now, please try again.\n");
			alert.showAndWait();
			System.exit(-1);
		}
	}

	/**
	 * Returns the main stage.
	 *
	 * @return primaryStage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;

	}

	/**
	 * @return the fileList
	 */
	public ObservableList<String> getFilesList() {
		return filesList;
	}

	/**
	 * @return the appliedFilters
	 */
	public ObservableList<Filter> getAppliedFilters() {
		return appliedFilters;
	}

	/**
	 * Gets the full image of the tooth.
	 *
	 * @return Image of the tooth.
	 */
	public Image getFullImage() {
		return fullImage;
	}

	/**
	 * Sets the full image of the tooth.
	 *
	 * @param fullImage
	 *            Full image of the tooth.
	 */
	public void setFullImage(Image fullImage) {
		this.fullImage = fullImage;
	}

	/**
	 * Gets the filtered image of the tooth.
	 *
	 * @return Image of the tooth.
	 */
	public Image getFilteredImage() {
		return filteredImage;
	}

	/**
	 * Sets the filtered image of the tooth.
	 *
	 * @param filteredImage
	 *            Filtered image of the tooth.
	 */
	public void setFilteredImage(Image filteredImage) {
		this.filteredImage = filteredImage;
	}

	/**
	 * Gets the cropped image of the tooth.
	 *
	 * @return Image of the tooth.
	 */
	public Image getCroppedImage() {
		return croppedImage;
	}

	/**
	 * Sets the cropped image of the tooth.
	 *
	 * @param croppedImage
	 *            Cropped image of the tooth.
	 */
	public void setCroppedImage(Image croppedImage) {
		this.croppedImage = croppedImage;
	}

	/**
	 * Gets the project data.
	 *
	 * @return Project with the project data.
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Sets the project data.
	 *
	 * @param project
	 *            Project with the data of a perikymata project.
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Saves the property of the last opened project file.
	 *
	 * @param file
	 *            Project file to store into preferences or null to remove the
	 *            preference.
	 */
	public void setProjectFilePathProperty(File file) {
		Properties properties = new Properties();
		try {

			if (file != null) {
				properties.setProperty("filePath", file.getPath());
			}
			FileOutputStream fout = new FileOutputStream("config.properties");
			properties.store(fout, null);
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception writing properties file.", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error saving properties file.");
			alert.setHeaderText("Error saving properties file.\n");
			alert.setContentText("Check that you have writing permissions on the folder\n"
					+ "that contains this application, so config.properties can be written.\n");
			alert.showAndWait();
		}
	}

	/**
	 * Loads the preference of the last opened project file.
	 *
	 * @return null if project wasn't found in the preferences, File of the last
	 *         opened project otherwise.
	 */
	public File getProjectFilePathProperty() {
		Properties properties = new Properties();
		try {
			String filePath = null;
			if (Paths.get("config.properties").toFile().exists()) {
				properties.load(new FileInputStream("config.properties"));
				filePath = properties.getProperty("filePath", null);
				if (filePath != null) {
					File f = new File(filePath);
					if (f.exists()) {
						return new File(filePath);
					}
				}
			}

		} catch (FileNotFoundException e) {
			this.getLogger().log(Level.WARNING, "Properties file doesn't exists.", e);

		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception occur opening properties file .", e);
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Error opening properties file.");
			alert.setHeaderText("Error loading opening properties file.\n");
			alert.setContentText(
					"If this problem persists, please delete config.properties" + " on the program folder.\n");
			alert.showAndWait();
		}
		return null;
	}

	/**
	 * Creates a new project (folder structure and project xml) by choosing a
	 * file-chooser.
	 *
	 * @return
	 */
	public Boolean createProject() {
		FileChooser fileChooser = new FileChooser();

		// Adds a filter that shows all the files..
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Project Folder", "*");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.initialFileNameProperty().set("Project_Name");

		// Shows the save dialog.
		File file = fileChooser.showSaveDialog(getPrimaryStage());

		if (file != null) {
			// destroys old data to create new.
			clearData();
			// Saves the project name.
			setProject(new Project());
			getProject().setProjectName(file.getName());

			if (tempUtil.isTempFolderValid(System.getProperty("java.io.tmpdir"))) {
				getProject().setTemporaryFolder("DEFAULT");
			} else {
				// Ask for the temp folder is system folder is not valid
				showTemporaryFolderSelection(true);
			}

			// Makes the folder structure.
			file.mkdir();
			// AMT Changed folder structure creation 30/01/2017
			new File(file.toString() + File.separator + "Fragments").mkdir();
			new File(file.toString() + File.separator + "Full_Image").mkdir();
			new File(file.toString() + File.separator + "Perikymata_Outputs").mkdir();
			new File(file.toString() + File.separator + "Cropped_Image").mkdir();
			setProjectPath(file.getPath());
			// Creates the XML project file.

			getPrimaryStage().setTitle("Perikymata - " + file.getName());
			makeProjectXml();
			showImageSelection();
			return true;
		}
		return false;

	}

	/**
	 * Opens a FileChooser to let the user select a perikymata project file
	 * (xml) to load.
	 *
	 * @return true/false
	 */
	public Boolean openProject() {
		FileChooser fileChooser = new FileChooser();
		// adds a XML project filter.
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Perikymata XML file (*.xml)", "*.xml");
		fileChooser.getExtensionFilters().add(extFilter);

		// shows the open project dialog.
		File file = fileChooser.showOpenDialog(getPrimaryStage());

		if (file != null) {
			// destroys old data to create new.
			clearData();

			loadProjectFromFile(file);
			setProjectPath(file.getParent());
			showImageSelection();
			return true;
		}
		return false;

	}

	/**
	 * @return the projectPath
	 */
	public String getProjectPath() {
		return projectPath;
	}

	/**
	 * @param projectPath
	 *            the projectPath to set
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;

	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Clear the data of all the variables to a new state to open or create a
	 * project.
	 */
	public void clearData() {
		this.appliedFilters.clear();
		this.filesList.clear();
		this.filteredImage = null;
		this.fullImage = null;
		this.croppedImage = null;
		this.project = null;
		this.projectPath = null;
	}
}
