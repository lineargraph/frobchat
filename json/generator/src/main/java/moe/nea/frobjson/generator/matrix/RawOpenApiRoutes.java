package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@ToString(exclude = {"servers"})
public class RawOpenApiRoutes {
	private String openapi;
	private Info info;
	private Map<String, Map<String, Route>> paths;
	private List<Server> servers = List.of();
	private Components components;

	public String guessBasePath() {
		return getServers()
			.stream()
			.map(RawOpenApiRoutes.Server::getVariables)
			.map(it -> it.get("basePath"))
			.filter(Objects::nonNull)
			.map(RawOpenApiRoutes.Variable::getDefault_)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse("");
	}

	@Getter
	@ToString
	public static class Server {
		private Map<String, Variable> variables;
		private Void url;
	}

	@Getter
	@ToString
	public static class Variable {
		@JsonProperty("enum")
		private Void enum_;
		@JsonProperty("default")
		private String default_;
	}


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
