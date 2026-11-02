package moe.nea.frobmatrix.ui;

import com.dlsc.gemsfx.PowerPane;
import javafx.geometry.Side;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import moe.nea.frobmatrix.ui.view.AccountOverview;
import moe.nea.frobmatrix.ui.view.SidePane;
import moe.nea.frobmatrix.ui.view.WelcomePanel;

public class FrobRootPanel extends PowerPane {

	public FrobRootPanel() {
		getHiddenSidesPane().setPinnedSide(Side.LEFT);
		getHiddenSidesPane().setLeft(SidePane.loadSidePanel());
		setContent(WelcomePanel.loadWelcomePanel());
	}
}
