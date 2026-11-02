package moe.nea.frobmatrix.ui;

import com.dlsc.gemsfx.PowerPane;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.ui.view.WelcomePanel;

public class FrobRootPanel extends PowerPane {

	public FrobRootPanel() {
		setContent(WelcomePanel.loadWelcomePanel());
	}
}
