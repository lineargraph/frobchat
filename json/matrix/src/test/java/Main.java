import moe.nea.frobchat.matrixapi.MatrixAuthentication;
import moe.nea.frobchat.matrixapi.MatrixClient;
import moe.nea.frobchat.matrixapi.operations.GetLoginFlows;

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
	}
}
