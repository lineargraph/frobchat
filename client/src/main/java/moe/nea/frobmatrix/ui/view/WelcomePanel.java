package moe.nea.frobmatrix.ui.view;

import com.dlsc.gemsfx.AvatarView;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import moe.nea.frobmatrix.fxutils.annotations.FXPropertyAccessors;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import moe.nea.frobmatrix.util.StreamUtil;
import moe.nea.frobmatrix.util.StringUtil;
import org.pdfsam.rxjavafx.observers.JavaFxObserver;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WelcomePanel implements Initializable {
	public final MatrixPlatform matrixPlatform = MatrixPlatform.matrixPlatform;
	private final StringProperty userName = new SimpleStringProperty();
	public AvatarView avatar;
	public Text welcome;


	public static Parent loadWelcomePanel() {
		return FXMLUtil.load(WelcomePanel.class, WelcomePanel::new);
	}

	public WelcomePanel() {
		userName.bind(JavaFxObserver.toBinding(matrixPlatform.matrixClient.getDisplayName().observeOn(JavaFxScheduler.platform())));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		welcome.textProperty().bind(userName.map(it -> "Welcome, " + it + "!"));
		avatar.initialsProperty().bind(userName.map(name -> StringUtil.getInitials(name, 2)));
		avatar.imageProperty()
			.bind(matrixPlatform.getAvatar(matrixPlatform.matrixClient.getAvatarUri()));
	}
}
