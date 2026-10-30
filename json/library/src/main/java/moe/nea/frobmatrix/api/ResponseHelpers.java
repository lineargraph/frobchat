package moe.nea.frobmatrix.api;

import moe.nea.frobmatrix.http.BooleanUriParameter;
import moe.nea.frobmatrix.Either;
import moe.nea.frobmatrix.http.IntegerUriParameter;
import moe.nea.frobmatrix.http.StringUriParameter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ResponseHelpers {
	private final static ObjectMapper MAPPER = new JsonMapper();

	public static <T> HttpRequest.BodyPublisher publishJson(T body, Class<T> bodyType) {
		return HttpRequest.BodyPublishers.ofByteArray(MAPPER.writeValueAsBytes(body));
	}

	public record ContentType(String contentType, Map<String, String> extraInformation) {
	}

	public static String urlEncodeParameter(String string) {
		return urlEncodeString(string);
	}

	public static String urlEncodeParameter(UriParameter parameter) {
		return urlEncodeString(parameter.asUriParameter());
	}

	public static String urlEncodeParameter(Either<? extends UriParameter, ? extends UriParameter> parameter) {
		return urlEncodeString(parameter.choose(UriParameter::asUriParameter, UriParameter::asUriParameter));
	}

	public static String urlEncodeNamedParameter(String name, @Nullable String value) {
		return urlEncodeNamedParameter(name, value == null ? null : new StringUriParameter(value));
	}

	public static String urlEncodeNamedParameter(String name, Boolean value) {
		return urlEncodeNamedParameter(name, value == null ? null : new BooleanUriParameter(value));
	}

	public static String urlEncodeNamedParameter(String name, Integer value) {
		return urlEncodeNamedParameter(name, value == null ? null : new IntegerUriParameter(value));
	}


	public static String urlEncodeNamedParameter(String name, List<String> values) {
		var stringBuilder = new StringBuilder();
		for (var value : values) {
			if (!stringBuilder.isEmpty())
				stringBuilder.append("&");
			stringBuilder
				.append(urlEncodeString(name))
				.append("=")
				.append(urlEncodeParameter(value));
		}
		return stringBuilder.toString();
	}

	public static String urlEncodeNamedParameter(String name, @Nullable UriParameter value) {
		if (value == null) return "";
		return urlEncodeString(name) + "=" + urlEncodeParameter(value.asUriParameter());
	}

	public static String urlEncodeString(String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8);
	}

	public static Map<String, String> parseExtraInformation(String extraInformation) {
		Map<String, String> bits = new HashMap<>();
		for (String part : extraInformation.split(";")) {
			var parts = part.split("=", 2);
			if (parts.length == 0 || parts[0].isBlank())
				continue;
			if (parts.length == 1) {
				bits.put(parts[0].trim(), "");
			} else {
				bits.put(parts[0].trim(), parts[1].trim());
			}
		}
		return bits;
	}

	public static ContentType parseContentType(HttpResponse.ResponseInfo responseInfo) {
		return parseContentType(responseInfo.headers().firstValue("content-type").orElse(""));
	}

	public static ContentType parseContentType(String contentType) {
		var parts = contentType.split(";", 2);
		if (parts.length == 2)
			return new ContentType(parts[0].trim().toLowerCase(Locale.ROOT), parseExtraInformation(parts[1]));
		if (parts.length == 1)
			return new ContentType(parts[0].trim().toLowerCase(Locale.ROOT), Map.of());
		return new ContentType("application/octet-stream", Map.of());
	}

	public static Charset getCharset(HttpResponse.ResponseInfo responseInfo) {
		var charsetName = parseContentType(responseInfo)
			.extraInformation()
			.getOrDefault("charset", "utf-8");
		return Charset.forName(charsetName, StandardCharsets.UTF_8);
	}

	public static <T> HttpResponse.BodySubscriber<T> unknownResponseError(
		HttpResponse.ResponseInfo responseInfo
	) {
		return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(getCharset(responseInfo)), it -> {
			throw new RuntimeException("Unknown response (" + responseInfo.statusCode() + "): " + it);
		});
	}

	public static <T, E> HttpResponse.BodySubscriber<T> parseErrorJson(
		HttpResponse.ResponseInfo responseInfo,
		Class<E> errorClass
	) {
		return HttpResponse.BodySubscribers.mapping(parseJson(responseInfo, errorClass), response ->
		{
			throw new RuntimeException("Received error: " + response);
		});
	}

	public static JsonNode toJsonValue(Object object) {
		return MAPPER.valueToTree(object);
	}

	public static <T> T fromJsonString(String text, Class<T> tClass) {
		return MAPPER.readValue(text, tClass);
	}

	public static <T> HttpResponse.BodySubscriber<T> parseJson(
		HttpResponse.ResponseInfo responseInfo,
		Class<T> dataClass
	) {
		return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), inputStream ->
			MAPPER.readValue(new InputStreamReader(inputStream, getCharset(responseInfo)), dataClass));
	}

	public static void checkPattern(String key, Pattern pattern) {
		if (!pattern.matcher(key).matches())
			throw new RuntimeException("Key " + key + " does not match the expected pattern " + pattern);
	}

}
