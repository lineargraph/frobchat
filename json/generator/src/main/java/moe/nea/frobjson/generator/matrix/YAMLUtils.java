package moe.nea.frobjson.generator.matrix;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class YAMLUtils {
	public final static ObjectMapper MAPPER = new YAMLMapper()
		.rebuild()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
		.build();

}
