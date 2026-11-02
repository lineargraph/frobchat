package moe.nea.frobmatrix.http;

import moe.nea.frobmatrix.Either;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.UriParameter;
import moe.nea.frobmatrix.util.StringUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@NullMarked
public class UriBuilder {
	private final String scheme;
	private final @Nullable String host;
	private final List<String> urlEncodedPathSegment = new ArrayList<>();
	private final List<BoundUriParameter> boundUriParameters = new ArrayList<>();

	public UriBuilder pathComponent(UriParameter userId) {
		pathSegment(ResponseHelpers.urlEncodeParameter(userId));
		return this;
	}

	public UriBuilder pathComponent(Either<? extends UriParameter, ? extends UriParameter> userId) {
		pathSegment(ResponseHelpers.urlEncodeParameter(userId));
		return this;
	}

	public UriBuilder pathComponent(String parameter) {
		pathSegment(ResponseHelpers.urlEncodeParameter(parameter));
		return this;
	}

	public record BoundUriParameter(String name, UriParameter value) {
	}

	public String getScheme() {
		return scheme;
	}

	public @Nullable String getHost() {
		return host;
	}

	public List<String> getUrlEncodedPathSegment() {
		return urlEncodedPathSegment;
	}

	public UriBuilder(String scheme, @Nullable String host) {
		this.scheme = scheme;
		this.host = host;
	}

	public UriBuilder appendQueryParameter(String name, @Nullable UriParameter value) {
		if (value == null) return this;
		return appendQueryParameter(new BoundUriParameter(name, value));
	}

	public UriBuilder appendQueryParameter(String name, @Nullable String value) {
		if (value == null) return this;
		return appendQueryParameter(name, new StringUriParameter(value));
	}

	public UriBuilder appendQueryParameter(String name, @Nullable Boolean value) {
		if (value == null) return this;
		return appendQueryParameter(name, new BooleanUriParameter(value));
	}

	public UriBuilder appendQueryParameter(String name, @Nullable Integer value) {
		if (value == null) return this;
		return appendQueryParameter(name, new IntegerUriParameter(value));
	}

	public UriBuilder appendQueryParameter(String name, @Nullable List<@Nullable String> values) {
		if (values == null) return this;
		for (var value : values) {
			appendQueryParameter(name, value);
		}
		return this;
	}

	/// Append a path segment
	public UriBuilder pathSegment(String path) {
		urlEncodedPathSegment.add(path);
		return this;
	}

	/// Append some path segments
	public UriBuilder pathSegments(String... paths) {
		for (var path : paths) {
			pathSegment(path);
		}
		return this;
	}

	/// Append an already encoded path
	public UriBuilder rawPath(String path) {
		for (var part : StringUtil.split(path, "/")) {
			if (!part.isEmpty())
				pathSegment(part);
		}
		return this;
	}

	private UriBuilder appendQueryParameter(@Nullable BoundUriParameter boundUriParameter) {
		if (boundUriParameter == null) return this;
		this.boundUriParameters.add(boundUriParameter);
		return this;
	}

	public String buildPath() {
		var sb = new StringBuilder();
		for (var part : urlEncodedPathSegment) {
			sb.append("/").append(part);
		}
		var isQueryStart = true;
		for (var boundUriParameter : boundUriParameters) {
			if (isQueryStart) {
				sb.append("?");
				isQueryStart = false;
			} else {
				sb.append("&");
			}
			sb.append(ResponseHelpers.urlEncodeString(boundUriParameter.name));
			if (!boundUriParameter.value.isUnitUriParameter()) {
				sb.append("=");
				sb.append(ResponseHelpers.urlEncodeParameter(boundUriParameter.value));
			}
		}
		return sb.toString();
	}

	public URI build() {
		return URI.create(scheme + ":" + (host == null ? "" : "//" + host) + buildPath());
	}

}
