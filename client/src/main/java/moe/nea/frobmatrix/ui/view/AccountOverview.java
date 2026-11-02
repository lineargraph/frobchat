package moe.nea.frobmatrix.ui.view;

import com.dlsc.gemsfx.AvatarView;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import moe.nea.frobmatrix.util.StringUtil;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountOverview extends HBox implements Initializable {
	final MatrixPlatform matrixPlatform = MatrixPlatform.current();
	public AvatarView avatar;
	public Text displayName;
	public Text userName;
	public Button settingsButton;

	public AccountOverview() {
		FXMLUtil.inject(this, AccountOverview.class);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		var userName = matrixPlatform.client().getLogInInfo().userId().asUriParameter();
		displayName.textProperty().bind(matrixPlatform.asBinding(matrixPlatform.client().getDisplayName()).orElse(userName));
		this.userName.textProperty().set(userName);
		avatar.initialsProperty().bind(displayName.textProperty().map(it -> StringUtil.getInitials(it, 2)));
		avatar.imageProperty().bind(matrixPlatform.getAvatar(matrixPlatform.client().getAvatarUri()));
//		settingsButton.setGraphic(
//			new FontIcon(Feather.SETTINGS)
//		);
	}
}
