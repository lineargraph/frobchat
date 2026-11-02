package moe.nea.frobmatrix.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class StringUtilTest {
	@Test
	void testStringSplitting() {
		assertThat(
			StringUtil.split(" A B  C ", " "),
			Matchers.contains("", "A", "B", "", "C", "")
		);
	}

}
