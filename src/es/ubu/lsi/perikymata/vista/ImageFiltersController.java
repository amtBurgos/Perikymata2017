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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.modelo.filters.Filter;
import es.ubu.lsi.perikymata.modelo.filters.Gauss;
import es.ubu.lsi.perikymata.modelo.filters.Prewitt;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Controller for the layout that filters images.
 * 
 * @author Sergio Chico Carrancio
 */
public class ImageFiltersController {
	//////////////////////// NotFXML variables////////////////////////////
	/**
	 * Used to modify the behaviour of the filters table.
	 */
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

	/**
	 * Reference to the main application
	 */
	private MainApp mainApp;

	/**
	 * Image used to apply the filters.
	 */
	private BufferedImage auxImage;

	///////////////////////// Imageviews Elements////////////////////////

	/**
	 * Reference to the filtered image.
	 */
	@FXML
	private ImageView filteredImage;

	/**
	 * Reference to the original image.
	 */
	@FXML
	private ImageView originalImage;

	/**
	 * Reference to the first Scrollpane.
	 */
	@FXML
	private ScrollPane scrollPane1;

	/**
	 * Reference to the second Scrollpane.
	 */
	@FXML
	private ScrollPane scrollPane2;

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

	//////////////////// Table Elements/////////////////////////
	/**
	 * List of operations to be executed.
	 */
	@FXML
	private TableView<Filter> filtersTable;

	/**
	 * Column of the table that shows the name of the filter.
	 */
	@FXML
	private TableColumn<Filter, String> filtersColumn;

	/**
	 * Column of the table that shows the arguments the filter.
	 */
	@FXML
	private TableColumn<Filter, String> argumentsColumn;

	/**
	 * Button to delete a selected filter.
	 */
	@FXML
	private Button deleteFilterButton;

	//////////////////////// Prewitt Filter///////////////////////
	/**
	 * Button to ad a new prewitt filter.
	 */
	@FXML
	private Button addPrewittButton;

	/**
	 * Slider for controlling the size of the prewitt mask.
	 */
	@FXML
	private Slider prewittSizeSlider;

	/**
	 * Label to show the exact size of the force on a prewitt filter.
	 */
	@FXML
	private Label prewittForceLevel;

	/**
	 * Slider for controlling the force of the prewitt mask.
	 */
	@FXML
	private Slider prewittForceSlider;

	/**
	 * Label to show the exact size of the mask on a prewitt filter.
	 */
	@FXML
	private Label prewittSizeLevel;

	////////////////////// Gaussian Filter/////////////////////////

	/**
	 * Button to ad a new gauss filter.
	 */
	@FXML
	private Button addGaussButton;

	/**
	 * Slider for controlling the sigma of the gauss filter.
	 */
	@FXML
	private Slider gaussSlider;

	/**
	 * Label to show the exact size of sigma on a gaussian filter.
	 */
	@FXML
	private Label gaussLevel;

	/////////////////////////////////////////////////////////////

	/**
	 * Initializes the components when the controller is loaded.
	 */
	@FXML
	private void initialize() {
		// binds scroll properties together to make both scrolls move at the
		// same time.
		scrollPane1.vvalueProperty().bindBidirectional(scrollPane2.vvalueProperty());
		scrollPane1.hvalueProperty().bindBidirectional(scrollPane2.hvalueProperty());

		// binds size of the two images to be the same.
		// originalImage.setPreserveRatio(true);
		originalImage.fitHeightProperty().bindBidirectional(filteredImage.fitHeightProperty());
		originalImage.fitWidthProperty().bindBidirectional(filteredImage.fitWidthProperty());

		scrollPane1.getContent().setCursor(
				new ImageCursor(new Image(this.getClass().getResource("/rsc/zoom_in.gif").toExternalForm())));
		scrollPane2.getContent().setCursor(scrollPane1.getContent().getCursor());
		scrollPane1.getContent().setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
					zoomPlusHandler();
				} else if (event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
					zoomMinusHandler();
				}
				event.consume();
			}
		});
		scrollPane2.getContent().setOnMouseClicked(scrollPane1.getContent().onMouseClickedProperty().get());

		// Handler that changes a labels to show the value of the sliders.
		gaussLevel.textProperty().bindBidirectional(gaussSlider.valueProperty(), new DecimalFormat("##.##"));
		prewittForceLevel.textProperty().bindBidirectional(prewittForceSlider.valueProperty(),
				new DecimalFormat("#.##"));
		prewittSizeLevel.textProperty().bindBidirectional(prewittSizeSlider.valueProperty(), new DecimalFormat("#"));
		status.setText("Idle");

		// Loads loading gif.
		loading.setImage(new Image(this.getClass().getResource("/rsc/482.gif").toExternalForm()));
		loading.setVisible(false);

		// Initialize the person table with the two columns.
		filtersColumn.setCellValueFactory(cellData -> cellData.getValue().getFiltername());
		argumentsColumn.setCellValueFactory(cellData -> cellData.getValue().getFilterArgs());

		// http://stackoverflow.com/a/28606524
		filtersTable.setRowFactory(tv -> {
			TableRow<Filter> row = new TableRow<>();

			row.setOnDragDetected(event -> {
				if (!row.isEmpty()) {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				}
			});

			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
					Filter draggedPerson = filtersTable.getItems().remove(draggedIndex);

					int dropIndex;

					if (row.isEmpty()) {
						dropIndex = filtersTable.getItems().size();
					} else {
						dropIndex = row.getIndex();
					}

					filtersTable.getItems().add(dropIndex, draggedPerson);

					event.setDropCompleted(true);
					filtersTable.getSelectionModel().select(dropIndex);
					event.consume();
				}
			});
			return row;
		});

	}

	@FXML
	private void zoomMinusHandler() {
		originalImage.setFitHeight(originalImage.getFitHeight() * 0.75);
	}

	@FXML
	private void zoomPlusHandler() {
		originalImage.setFitHeight(originalImage.getFitHeight() * 1.25);
	}

	/**
	 * Handler that adds a prewitt filter to the list of filters.
	 */
	@FXML
	private void addPrewittFilter() {
		mainApp.getAppliedFilters().add(new Prewitt((int) prewittSizeSlider.getValue(), prewittForceSlider.getValue()));

	}

	/**
	 * Handler that adds a gaussian filter to the list of filters.
	 */
	@FXML
	private void addGaussianFilter() {
		mainApp.getAppliedFilters().add(new Gauss(gaussSlider.getValue()));
	}

	/**
	 * Handler that removes a filter from the list.
	 */
	@FXML
	private void handleRemoveFilter() {
		int selectedIndex = filtersTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			filtersTable.getItems().remove(selectedIndex);
		} else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("No Filter Selected");
			alert.setContentText("Please select a filter in the table to delete it");
			alert.showAndWait();
		}
	}

	/**
	 * Handler that removes a filter from the list.
	 */
	@FXML
	private void handleRunFilters() {

		// Saves a copy of the original image to apply filters
		auxImage = SwingFXUtils.fromFXImage(this.originalImage.getImage(), null);
		disableComponents();
		changeStatus("Applying filters, please wait");
		loading.setVisible(true);
		new Thread(() -> {
			ObservableList<Filter> f = this.filtersTable.getItems();
			for (int i = 0; i < f.size(); i++) {
				changeStatus("Aplying " + f.get(i).getFiltername().getValue() + ". " + (i + 1) + "/" + f.size());
				this.auxImage = f.get(i).run(auxImage);
			}

			filteredImage.setImage(SwingFXUtils.toFXImage(auxImage, null));
			this.mainApp.setFilteredImage(SwingFXUtils.toFXImage(auxImage, null));
			changeStatus("Filters apply completed! Idle.");

			loading.setVisible(false);
			this.enableComponents();
		}).start();

	}

	/**
	 * Disables the components while the algorithm is running.
	 */
	private void disableComponents() {
		mainApp.getRootLayout().setDisable(true);
	}

	/**
	 * enables the components when the algorithm has ended.
	 */
	private void enableComponents() {
		mainApp.getRootLayout().setDisable(false);
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
			originalImage.setImage(mainApp.getFullImage());
			filteredImage.setImage(mainApp.getFilteredImage());

			originalImage.setFitHeight(originalImage.getImage().getHeight());

			// Add observable list data to the table
			filtersTable.setItems(mainApp.getAppliedFilters());

			// Saves a copy of the original image to apply filters
			auxImage = SwingFXUtils.fromFXImage(this.originalImage.getImage(), null);
		}
	}

	/**
	 * Handler that changes to the perikymata counting stage when called.
	 */
	@FXML
	private void nextScreen() {
		mainApp.getProject().setFilterList(mainApp.getAppliedFilters());
		saveToFile(filteredImage.getImage());
		mainApp.makeProjectXml();
		mainApp.showPerikymataCount();
	}

	/**
	 * Saves the filtered image to disk.
	 * 
	 * @param image
	 *            Filtered image.
	 */
	private void saveToFile(Image image) {
		File outputFile = Paths.get(mainApp.getProjectPath(), "Full_Image", "Filtered_Image.png").toFile();
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			mainApp.getLogger().log(Level.SEVERE, "Filtered image cannot be saved.", e);
		}
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
