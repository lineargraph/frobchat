import moe.nea.frobchat.matrixapi.MatrixAuthentication;
import moe.nea.frobchat.matrixapi.MatrixClient;
import moe.nea.frobchat.matrixapi.operations.GetLoginFlows;
import moe.nea.frobchat.matrixapi.operations.GetWellknown;
import moe.nea.frobjson.openapi.Operation;

import java.net.http.HttpClient;

void main() {
	var httpClient = HttpClient.newHttpClient();
	var client = MatrixClient.client(
		httpClient,
		MatrixAuthentication.anon("https://nea.moe")
	);
	var result = client.getWellknown().join();
	var body = result.body();
	System.out.println(body.mHomeserver());
	client = MatrixClient.client(
		httpClient,
		MatrixAuthentication.anon("https://matrix.nea.moe")
	);
	System.out.println(((GetLoginFlows.Status200) client.getLoginFlows().join()).body());
}
