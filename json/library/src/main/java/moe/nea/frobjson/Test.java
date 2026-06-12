package moe.nea.frobjson;

import moe.nea.frobjson.model.DiscoveryInformation;
import moe.nea.frobjson.model.HomeserverInformation;

public class Test {

	static void main() {
		System.out.println(new DiscoveryInformation(
			new HomeserverInformation(
				"https://matrix.nea.moe"
			),
			null
		).asJson());
	}
}
