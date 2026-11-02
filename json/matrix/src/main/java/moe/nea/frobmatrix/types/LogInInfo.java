package moe.nea.frobmatrix.types;

import moe.nea.frobmatrix.MxUserId;

public record LogInInfo(
	MxUserId userId,
	String deviceId,
	boolean isGuest
) {
}
