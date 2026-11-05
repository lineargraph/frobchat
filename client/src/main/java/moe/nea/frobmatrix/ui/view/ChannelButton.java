package moe.nea.frobmatrix.ui.view;

import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import moe.nea.frobmatrix.types.room.MatrixRoom;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class ChannelButton extends AnchorPane implements Initializable {
	private final MatrixRoom matrixRoom;
	MatrixPlatform matrixPlatform = MatrixPlatform.current();
	public FontIcon icon;
	public Text channelName;

	public ChannelButton(MatrixRoom matrixRoom) {
		this.matrixRoom = matrixRoom;
		FXMLUtil.inject(this, ChannelButton.class);
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		channelName.textProperty().set(matrixRoom.roomId().asUriParameter());
//		icon.iconCodeProperty()
//			.bind();
	}
}
