package moe.nea.frobjson.generator.matrix;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString(exclude = {"servers"})
public class RawOpenApiRoutes {
	private String openapi;
	private Info info;
	private Map<String, Map<String, Route>> paths;
	private Void servers;

	@Getter
	@ToString
	public static class Route {
		private String summary;
		private String description;
		private String operationId;
		private Map<String, Response> responses;
		private List<String> tags;
	}

	@Getter
	@ToString
	public static class Response {
		private String description;
		private Map<String, ResponseContent> content;
	}

	@Getter
	@ToString
	public static class ResponseContent {
		private IndirectJsonSchema schema;
	}

	@Getter
	@ToString
	public static class Info {
		private String title;
		private String version;
	}
}
