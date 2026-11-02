package moe.nea.frobmatrix.api.clientserver.authedcontentrepo;

import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.Route;
import moe.nea.frobmatrix.api.clientserver.definitions.errors.ratelimited.RateLimitError;
import moe.nea.frobmatrix.http.UriBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

public class GetContentAuthed extends Route<GetContentAuthed.MediaDownload> {

	private final String serverName;
	private final String mediaId;
	private final Integer timeout;

	public GetContentAuthed(String serverName, String mediaId, Integer timeout) {
		this.serverName = serverName;
		this.mediaId = mediaId;
		this.timeout = timeout;
	}

	@Override protected HttpResponse.BodySubscriber<Supplier<GetContentAuthed.MediaDownload>> handleResponseInfo(HttpResponse.ResponseInfo responseInfo) {
		return switch (responseInfo.statusCode()) {
			case 200 -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), it -> () -> {
				var contentType = ResponseHelpers.parseContentType(responseInfo);
				var contentDisposition = ResponseHelpers.parseContentDisposition(responseInfo.headers().firstValue("content-disposition").orElse(""));
				return new MediaDownload(contentType, contentDisposition, it);
			});
			case 429 -> ResponseHelpers.parseErrorJson(responseInfo, RateLimitError.class);
			default -> ResponseHelpers.unknownResponseError(responseInfo);
		};
	}

	@Override public Method method() {
		return Method.GET;
	}

	@Override public UriBuilder appendUriTo(UriBuilder uriBuilder) {
		return uriBuilder.rawPath("/media/download").pathComponent(serverName).pathComponent(mediaId).appendQueryParameter("timeout_ms", timeout);
	}

	@Override public String rawPath() {
		return "/media/download/{mediaId}";
	}

	public static class MediaDownload implements Closeable {
		private final ResponseHelpers.ContentType contentType;
		private final ResponseHelpers.ContentDisposition contentDisposition;
		private final InputStream inputStream;

		public MediaDownload(ResponseHelpers.ContentType contentType, ResponseHelpers.ContentDisposition contentDisposition, InputStream inputStream) {
			this.contentType = contentType;
			this.contentDisposition = contentDisposition;
			this.inputStream = inputStream;
		}

		public ResponseHelpers.ContentType getContentType() {
			return contentType;
		}

		public ResponseHelpers.ContentDisposition getContentDisposition() {
			return contentDisposition;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		@Override public void close() throws IOException {
			inputStream.close();
		}
	}

}
