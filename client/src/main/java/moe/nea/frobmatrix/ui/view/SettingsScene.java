package moe.nea.frobmatrix.ui.view;

import atlantafx.base.controls.*;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import moe.nea.frobmatrix.ui.util.FXMLUtil;
import moe.nea.frobmatrix.ui.view.settings.Setting;
import moe.nea.frobmatrix.ui.view.settings.SettingsPage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class SettingsScene extends BorderPane implements Initializable {
	public Sidebar<Setting> sidebar;
	public ScrollPane scrollPane;

	private ListProperty<SettingsPage> settings = new SimpleListProperty<>(FXCollections.observableArrayList());

	@FXML
	public ObservableList<SettingsPage> getSettings() {
		return settings.get();
	}

	public SettingsScene() {
//		settings.setAll(
//			new SettingsPage(
//				"Account",
//				List.of(
//					new Setting("Username"),
//					new Setting("Avatar"),
//					new Setting("Pronouns")
//					)
//			)
//		);
		FXMLUtil.inject(this, SettingsScene.class);
	}

	public void scrollIntoView(Node node) {
		var contentBounds = scrollPane.getContent().getBoundsInLocal();
		var nodeBounds = node.getBoundsInParent();
		scrollPane.setVvalue(nodeBounds.getMinY() / contentBounds.getHeight());
		node.requestFocus();
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		sidebar.setHeader(new SidebarHeader<>("Settings", new FontIcon(Feather.SETTINGS)));
		sidebar.addEventHandler(SidebarEvent.<Setting>itemClick(), e -> {
			Setting setting = e.getItem().getValue();
			System.out.println("Clicked on settings tab: " + setting);
		});
		sidebar.setPrefWidth(300);
		sidebar
			.getItems()
			.setAll(settings.stream()
				.flatMap(page -> Stream.concat(
					Stream.of(new SidebarGroup<Setting>(page.getLabel())),
					page.getSettings().stream().map(it -> {
						var nav = new SidebarNav<Setting>(null);
						nav.textProperty().bind(it.text);
						nav.setValue(it);
						return nav;
					})
				))
				.toList());
	}
}
