package moe.nea.frobjson.openapi;

import com.google.gson.JsonElement;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@NullMarked
public interface Operation<P extends Operation.Parameters, B extends Operation.Body, R> {
	String path();

	String method();

	String path(P parameters);

	R fromResponse(int statusCode, JsonElement element);

	sealed interface Body permits EmptyBody, JsonBody {
	}

	non-sealed interface JsonBody<T> extends Body {
		@Nullable T body();

		@Nullable JsonElement asJson();
	}

	final class EmptyBody implements Body {
	}

	EmptyBody EMPTY_BODY = new EmptyBody();

	interface Parameters {
		default QueryParameters queryParameters() {
			return QueryParameters.empty();
		}

		default String pathParameter(String name) {
			throw new RuntimeException("Unknown path parameter: " + name);
		}
	}
}
