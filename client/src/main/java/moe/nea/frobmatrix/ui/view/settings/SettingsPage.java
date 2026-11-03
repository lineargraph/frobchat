package moe.nea.frobmatrix.ui.view.settings;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.beans.BeanProperty;
import java.util.List;

@DefaultProperty("settings")
public class SettingsPage {
	private ListProperty<Setting> settings = new SimpleListProperty<>(FXCollections.observableArrayList());
	private StringProperty label = new SimpleStringProperty();

	public SettingsPage() {
	}

	public SettingsPage(String label, List<Setting> settings) {
		setLabel(label);
		this.settings.setAll(settings);
	}


	public StringProperty labelProperty() {
		return label;
	}

	public String getLabel() {
		return label.get();
	}

	public void setLabel(String label) {
		this.label.set(label);
	}

	public ObservableList<Setting> getSettings() {
		return settings.get();
	}
}
