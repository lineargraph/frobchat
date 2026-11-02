package moe.nea.frobmatrix.storage;

import moe.nea.frobmatrix.util.Pair;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@NullMarked
public interface StorageBackend {
	void storeRoomState(String roomId, Pair<String, String> key, JsonNode value);

	@Nullable Map<Pair<String, String>, JsonNode> getRoomState(String roomId);

	void storeSingleValue(String key, JsonNode value);

	@Nullable JsonNode getSingleValue(String key);
}
