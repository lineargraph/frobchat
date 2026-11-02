package moe.nea.frobmatrix.ui;

import atlantafx.base.theme.PrimerDark;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrobApplication extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		MatrixPlatform.login();
		var root = new FrobRootPanel();
		var scene = new Scene(root);

		Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
		GemsFXAtlantaFX.applyTo(scene);

		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
