package moe.nea.frobmatrix.ui.view.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Setting {
	public Setting(String text) {
		setText(text);
	}

	public Setting() {
	}

	@Override public String toString() {
		return getText();
	}

	public StringProperty text = new SimpleStringProperty();

	public String getText() {
		return text.get();
	}

	public void setText(String text) {
		this.text.set(text);
	}

	public StringProperty textProperty() {
		return text;
	}
}
