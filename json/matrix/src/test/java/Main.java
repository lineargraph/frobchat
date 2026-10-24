import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.Authentication;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.api.clientserver.routes.wellknown.GetWellknown;
import moe.nea.frobmatrix.api.clientserver.routes.whoami.GetTokenOwner;

import java.net.http.HttpClient;

public class Main {
	public static void main(String[] args) {
		var httpClient = HttpClient.newHttpClient();
		var matrixClient = new MatrixRESTClient(httpClient, "nea.moe", Authentication.none());
		System.out.println(new GetWellknown().execute(matrixClient).body());
		var TEST_TOKEN = "syt_dGVzdA_oUuqBZxodLPeyCoEyTLR_22RpPL";
		var TEST_USER = MxUserId.parse("@test:nea.moe");
		matrixClient = new MatrixRESTClient(httpClient, "matrix.nea.moe", Authentication.token(TEST_TOKEN));
		System.out.println(new GetTokenOwner().execute(matrixClient).body());
//		var client = MatrixClient.client(
//			httpClient,
//			MatrixAuthentication.anon("https://nea.moe")
//		);
//		var result = client.getWellknown().join();
//		System.out.println(result.mHomeserver());
//		client = MatrixClient.client(
//			httpClient,
//			MatrixAuthentication.anon("https://matrix.nea.moe")
//		);
//		System.out.println(client.getLoginFlows().join());
//		System.out.println(new UserIdentifierExt.MIdUser("nea").asJson());
//		System.out.println(UserIdentifierExt.downcast(UserIdentifier.fromJson(new UserIdentifierExt.MIdUser("nea").asJson())));
	}
}
