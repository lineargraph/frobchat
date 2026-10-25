package moe.nea.frobmatrix.api.clientserver.definitions.useridentifier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = UserIdentifier.MatrixUserID.class, name = "m.id.user"),
	@JsonSubTypes.Type(value = UserIdentifier.ThirdPartyID.class, name = "m.id.thirdparty"),
	@JsonSubTypes.Type(value = UserIdentifier.PhoneNumber.class, name = "m.id.phone")
})
public sealed class UserIdentifier {
	public static final class ThirdPartyID extends UserIdentifier {
		@JsonProperty(required = true)
		private String medium;
		@JsonProperty(required = true)
		private String address;

		public String medium() {
			return this.medium;
		}

		public String address() {
			return this.address;
		}

		ThirdPartyID() {
		}

		public ThirdPartyID(String medium, String address) {
			this.medium = medium;
			this.address = address;
		}
	}

	public static final class MatrixUserID extends UserIdentifier {
		@JsonProperty(required = true)
		private String user;

		MatrixUserID() {
		}

		public MatrixUserID(String user) {
			this.user = user;
		}
	}

	public static final class PhoneNumber extends UserIdentifier {
		@JsonProperty(required = true)
		private String country;

		public String country() {
			return this.country;
		}

		@JsonProperty(required = true)
		private String phone;

		public String phone() {
			return this.phone;
		}

		PhoneNumber() {
		}

		public PhoneNumber(String country, String phone) {
			this.country = country;
			this.phone = phone;
		}
	}
}
