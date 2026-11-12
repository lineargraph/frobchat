package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;

import java.util.Map;

public interface AccountData extends HasMatrixClient {
	StateEventMap events();

	Observable<Map<MxRoomId, MxUserId>> directMessages();
}
