import moe.nea.frobchat.matrixapi.MatrixAuthentication;
import moe.nea.frobchat.matrixapi.MatrixClient;
import moe.nea.frobchat.matrixapi.model.UserIdentifier;
import moe.nea.frobchat.matrixapi.model.UserIdentifierExt;

import java.net.http.HttpClient;

public class Main {
	public static void main(String[] args) {
		var httpClient = HttpClient.newHttpClient();
		var client = MatrixClient.client(
			httpClient,
			MatrixAuthentication.anon("https://nea.moe")
		);
		var result = client.getWellknown().join();
		System.out.println(result.mHomeserver());
		client = MatrixClient.client(
			httpClient,
			MatrixAuthentication.anon("https://matrix.nea.moe")
		);
		System.out.println(client.getLoginFlows().join());
		System.out.println(new UserIdentifierExt.MIdUser("nea").asJson());
		System.out.println(UserIdentifierExt.downcast(UserIdentifier.fromJson(new UserIdentifierExt.MIdUser("nea").asJson())));
	}
}
