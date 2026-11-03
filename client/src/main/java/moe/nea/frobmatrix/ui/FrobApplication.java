package moe.nea.frobmatrix.ui;

import atlantafx.base.theme.PrimerDark;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import moe.nea.frobmatrix.ui.view.SettingsScene;

public class FrobApplication extends Application {
	static FrobApplication instance;
	Stage primaryStage;
	Stage settingsStage;

	public static FrobApplication instance() {
		return instance;
	}

	public void showSettings() {
		if (settingsStage == null || !settingsStage.isShowing()) {
			settingsStage = new Stage();
			settingsStage.initOwner(primaryStage);
			settingsStage.setScene(createScene(new SettingsScene()));
		}
		settingsStage.show();
		settingsStage.requestFocus();
	}

	public Scene createScene(Parent node) {
		var scene = new Scene(node);
		GemsFXAtlantaFX.applyTo(scene);
		return scene;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;
		MatrixPlatform.login();
		Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

		this.primaryStage = primaryStage;

		primaryStage.setScene(createScene(new FrobRootPanel()));
		primaryStage.show();
	}
}
