package moe.nea.frobmatrix.storage;

import moe.nea.frobmatrix.util.Pair;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@NullMarked
public class MemoryStorage implements StorageBackend, Serializable {
	Map<String, JsonNode> kv = new HashMap<>();
	Map<String, Map<Pair<String, String>, JsonNode>> roomState = new HashMap<>();

	public void save(Path file) {
		try {
			Files.createDirectories(file.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (
			var outputStream = Files.newOutputStream(file);
			var objectOutputStream = new ObjectOutputStream(outputStream);
		) {
			objectOutputStream.writeObject(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/// For testing purposes only, we do _not_ want to use java.io.serialization for storage
	public static MemoryStorage tryLoad(Path file) {
		try (
			var inputStream = Files.newInputStream(file);
			var objectInputStream = new ObjectInputStream(inputStream);
		) {
			return (MemoryStorage) objectInputStream.readObject();
		} catch (NoSuchFileException e) {
			return new MemoryStorage();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void storeRoomState(String roomId, Pair<String, String> key, JsonNode value) {
		roomState.computeIfAbsent(roomId, ignored -> new HashMap<>())
			.put(key, value);
	}

	@Override
	public @Nullable Map<Pair<String, String>, JsonNode> getRoomState(String roomId) {
		return roomState.get(roomId);
	}

	@Override
	public void storeSingleValue(String key, JsonNode value) {
		kv.put(key, value);
	}

	@Override
	public @Nullable JsonNode getSingleValue(String key) {
		return kv.get(key);
	}
}
