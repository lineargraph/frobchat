package moe.nea.frobmatrix.ui.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import moe.nea.frobmatrix.fxutils.annotations.FXPropertyAccessors;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import org.pdfsam.rxjavafx.observers.JavaFxObserver;

import java.net.URL;
import java.util.ResourceBundle;

public class WelcomePanel extends WelcomePanelProps implements Initializable {
	private final StringProperty displayName = new SimpleStringProperty();
	public ImageView image;

	@FXPropertyAccessors
	public StringProperty displayNameProperty() {
		return displayName;
	}

	public static Parent loadWelcomePanel() {
		return FXMLUtil.load(WelcomePanel.class, WelcomePanel::new);
	}

	public WelcomePanel() {
		displayName.bind(JavaFxObserver.toBinding(MatrixPlatform.matrixClient.getDisplayName())
			.map(it -> "Welcome, " + it + "!"));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		image.imageProperty()
			.bind(JavaFxObserver.toBinding(MatrixPlatform.matrixClient.getAvatarUri()));
	}
}
