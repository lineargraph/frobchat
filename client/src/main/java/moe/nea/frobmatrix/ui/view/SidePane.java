package moe.nea.frobmatrix.ui.view;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class SidePane implements Initializable {
	public final MatrixPlatform matrixPlatform = MatrixPlatform.matrixPlatform;

	public static Parent loadSidePanel() {
		return FXMLUtil.load(SidePane.class, SidePane::new);
	}

	@Override public void initialize(URL location, ResourceBundle resources) {

	}
}
