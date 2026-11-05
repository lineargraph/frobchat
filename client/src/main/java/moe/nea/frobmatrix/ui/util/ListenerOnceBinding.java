package moe.nea.frobmatrix.ui.util;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;


/// Binding that disposes after a listener gets removed. Use only if binding to
public class ListenerOnceBinding<T> implements Binding<T> {
	private Binding<T> binding;

	public ListenerOnceBinding(Binding<T> binding) {
		this.binding = binding;
	}

	@Override public boolean isValid() {
		if (binding == null)
			return false;
		return binding.isValid();
	}

	@Override public void invalidate() {
		if (binding != null)
			binding.invalidate();
	}

	@Override public ObservableList<?> getDependencies() {
		if (binding == null) throw new UnsupportedOperationException();
		return binding.getDependencies();
	}

	@Override public void dispose() {
		if (binding != null) {
			binding.dispose();
			binding = null;
		}
	}

	@Override public void addListener(ChangeListener<? super T> listener) {
		if (binding == null) throw new UnsupportedOperationException();
		binding.addListener(listener);
	}

	@Override public void removeListener(ChangeListener<? super T> listener) {
		if (binding == null) throw new UnsupportedOperationException();
		binding.removeListener(listener);
		dispose();
	}

	@Override public T getValue() {
		if (binding == null) throw new UnsupportedOperationException();
		return binding.getValue();
	}

	@Override public void addListener(InvalidationListener listener) {
		if (binding == null) throw new UnsupportedOperationException();
		binding.addListener(listener);
	}

	@Override public void removeListener(InvalidationListener listener) {
		if (binding == null) throw new UnsupportedOperationException();
		binding.removeListener(listener);
		dispose();
	}
}
