package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.clientserver.definitions.eventbatch.EventBatch;
import moe.nea.frobmatrix.event.EventTypes;
import moe.nea.frobmatrix.event.types.DirectChatMapping;
import moe.nea.frobmatrix.eventschemas.schema.coreeventschema.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class AccountDataImpl implements AccountData {
	private static final Logger log = LoggerFactory.getLogger(AccountDataImpl.class);
	private final MatrixClient client;
	private final StateEventMapImpl stateEventMap = new StateEventMapImpl();
	private final Observable<Map<MxRoomId, MxUserId>> userByDm;

	public @NonNull Observable<Map<MxRoomId, MxUserId>> directMessages() {
		return userByDm.hide();
	}

	public AccountDataImpl(MatrixClient client) {
		this.client = client;

		userByDm = (events().observeStateEvent(DirectChatMapping.TYPE)
			.map(opt -> {
				return opt.map(direct -> direct.additionalProperties().entrySet().stream()
					.flatMap(entry -> entry.getValue().stream().map(channel -> Map.entry(MxRoomId.parse(channel), entry.getKey())))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).orElse(Map.of());
			})
			.map(it -> {
				System.out.println("Set direct messages to " + it);
				return it;
			}))
			.distinctUntilChanged()
			.replay(1)
			.refCount();
	}

	@Override public StateEventMap events() {
		return stateEventMap;
	}

	@Override public MatrixClient matrixClient() {
		return client;
	}

	public void setAccountData(EventBatch eventBatch) {
		if (eventBatch == null) return;
		for (Event event : eventBatch.events()) {
			log.info("Setting account data of type {}", event.type());
			stateEventMap.setStateEvent(EventTypes.parseContent(event.type(), event.content()), KeyedStateEventMap.DEFAULT_KEY);
		}
	}
}
