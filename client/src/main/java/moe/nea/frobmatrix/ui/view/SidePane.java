package moe.nea.frobmatrix.ui.view;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import moe.nea.frobmatrix.types.room.MatrixRoom;
import moe.nea.frobmatrix.ui.MatrixPlatform;
import moe.nea.frobmatrix.ui.util.FXMLUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SidePane implements Initializable {

	public final MatrixPlatform matrixPlatform = MatrixPlatform.matrixPlatform;
	public VBox channelView;

	public static Parent loadSidePanel() {
		return FXMLUtil.load(SidePane.class, SidePane::new);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		var channelButtons = matrixPlatform
			.asBinding(matrixPlatform.client().observeRooms())
			.map(rooms -> rooms.values()
				.stream()
				.sorted(MatrixRoom.ROOM_ID_COMPARATOR)
				.map(ChannelButton::new)
				.toList());
		ObservableList<ChannelButton> channelButtonList = FXCollections.observableArrayList();
		ChangeListener<List<ChannelButton>> changeListener = (observable, oldValue, newValue) -> {
			if (newValue == null)
				channelButtonList.clear();
			else
				channelButtonList.setAll(newValue);
		};

		changeListener.changed(null, null, channelButtons.getValue());
		channelButtons.addListener(new WeakChangeListener<>(changeListener));
		Bindings.bindContent(channelView.getChildren(), channelButtonList);
		channelView.getProperties().put("changeListener", changeListener);
	}
}
