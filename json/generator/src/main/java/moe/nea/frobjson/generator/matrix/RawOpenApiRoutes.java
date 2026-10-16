package moe.nea.frobjson.generator.matrix;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString(exclude = {"servers"})
public class RawOpenApiRoutes {
	private String openapi;
	private Info info;
	private Map<String, Map<String, Route>> paths;
	private Void servers;
	private Components components;

	@Getter
	@ToString
	public static class Components {
		private Void securitySchemes;
	}

	@Getter
	@ToString
	public static class Route {
		private String summary;
		private String description;
		private String operationId;
		private Map<Integer, Response> responses;
		private List<String> tags;
		private Void security;
	}

	@Getter
	@ToString
	public static class Response {
		private String description;
		private Map<String, ResponseContent> content = new HashMap<>();
	}

	@Getter
	@ToString
	public static class ResponseContent {
		private MemberType schema;
		private Void examples;
	}

	@Getter
	@ToString
	public static class Info {
		private String title;
		private String version;
	}
}
