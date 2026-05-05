package utils;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Reactive<T> {
    private volatile T value;
    private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    public Reactive() {
    }

    public Reactive(T initial) {
        this.value = initial;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        T old = this.value;
        if (!Objects.equals(old, newValue)) {
            this.value = newValue;
            listeners.forEach(l -> l.accept(newValue));
        }
    }

    public Runnable subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
        return () -> listeners.remove(listener);
    }

    public interface Observable<T> {
        T get();

        Runnable subscribe(Consumer<T> listener);
    }

    public Observable<T> readOnly() {
        return new Observable<>() {
            @Override
            public T get() {
                return Reactive.this.get();
            }

            @Override
            public Runnable subscribe(Consumer<T> l) {
                return Reactive.this.subscribe(l);
            }
        };
    }
}