package moe.nea.frobmatrix;

import io.reactivex.rxjava3.core.Single;
import moe.nea.frobmatrix.api.clientserver.authedcontentrepo.GetContentAuthed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileCache {
	public static Logger log = LoggerFactory.getLogger(FileCache.class);
	private final MatrixClient client;
	private final Path path;
	private final ExecutorService imageDownloadExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
		.name("Matrix ImageDownloader")
		.factory());
	private final Map<MxMxcUri, Single<Path>> downloads = new HashMap<>();

	public FileCache(MatrixClient client) {
		this.client = client;
		this.path = Path.of("build/image-cache");
	}

	public Single<Path> download(MxMxcUri uri) {
		synchronized (downloads) {
			return downloads.computeIfAbsent(uri,
				it -> Single.fromFuture(CompletableFuture.supplyAsync(() -> downloadImage0(it), imageDownloadExecutor))
			);
		}
	}

	private Path downloadImage0(MxMxcUri uri) {
		// TODO: ensure this is not evil
		Path cachePath = this.path.resolve(uri.host()).resolve(uri.opaqueId());
		if (Files.exists(cachePath)) {
			return cachePath;
		}
		log.info("Downloading file {}", uri);
		try (var mediaDownload = client.getRestClient().execute(new GetContentAuthed(uri.host(), uri.opaqueId(), 20000))) {
			Files.createDirectories(cachePath.getParent());
			var downloadPath = cachePath.resolveSibling(uri.opaqueId() + "_download_" + UUID.randomUUID());
			Files.copy(mediaDownload.getInputStream(), downloadPath);
			log.info("Copying saved file from {} to {}", downloadPath, cachePath);
			Files.move(downloadPath, cachePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			return cachePath;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
