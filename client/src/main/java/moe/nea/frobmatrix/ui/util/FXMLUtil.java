package moe.nea.frobmatrix.ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import moe.nea.frobmatrix.ui.view.WelcomePanel;

import java.io.IOException;
import java.util.function.Supplier;

public class FXMLUtil {
	public static <T> void inject(T view, Class<T> tClass) {
		var loader = new FXMLLoader(tClass.getResource(tClass.getSimpleName() + ".fxml"));
		loader.setControllerFactory(type -> {
			if (type == tClass)
				return view;
			throw new IllegalArgumentException("Unknown controller type " + type);
		});
		loader.setRoot(view);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Parent load(Class<T> controller, Supplier<T> constructor) {
		var loader = new FXMLLoader(controller.getResource(controller.getSimpleName() + ".fxml"));
		loader.setControllerFactory(type -> {
			if (type == controller)
				return constructor.get();
			throw new IllegalArgumentException("Unknown controller type " + type);
		});
		try {
			return loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
