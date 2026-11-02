package moe.nea.frobmatrix.event;

public interface Event {
	RawEventContent content();

	String type();
}
