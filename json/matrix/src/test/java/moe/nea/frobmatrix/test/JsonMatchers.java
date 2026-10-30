package moe.nea.frobmatrix.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

public class JsonMatchers {
	public static Matcher<JsonNode> hasString(Matcher<String> stringMatcher) {
		return new FeatureMatcher<>(stringMatcher, "with asString()", "asString()") {
			@Override
			protected String featureValueOf(JsonNode actual) {
				return actual.isMissingNode() ? null : actual.asString();
			}
		};
	}

	public static Matcher<JsonNode> nodeExists() {
		return new BaseMatcher<JsonNode>() {
			@Override
			public boolean matches(Object actual) {
				return !((JsonNode) actual).isMissingNode();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("to exist");
			}
		};
	}

	public static Matcher<JsonNode> hasProperty(String path, Matcher<JsonNode> matcher) {
		return hasProperty(JsonPointer.compile(path), matcher);
	}

	public static Matcher<JsonNode> hasProperty(JsonPointer path, Matcher<JsonNode> matcher) {
		return new FeatureMatcher<>(matcher, "with nodeAt(" + path + ")", "nodeAt(" + path + ")") {
			@Override
			protected JsonNode featureValueOf(JsonNode actual) {
				return actual.at(path);
			}
		};
	}


}
