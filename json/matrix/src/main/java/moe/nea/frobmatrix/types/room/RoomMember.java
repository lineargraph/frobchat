package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.types.MatrixObject;

import java.util.Optional;

public interface RoomMember extends MatrixObject {
	Observable<Optional<String>> name();

	Observable<String> title();

	MxUserId userId();
}
