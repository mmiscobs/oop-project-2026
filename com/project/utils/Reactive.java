package com.project.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        if (!listeners.contains(listener))
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

    public static class ReactiveArrayList<T> extends ArrayList<T> implements Observable<List<T>> {
        public boolean add(T e) {
            return callListeners(super.add(e));
        }

        public boolean remove(Object o) {
            return callListeners(super.remove(o));
        }

        public boolean addAll(Collection<? extends T> c) {
            return callListeners(super.addAll(c));
        }

        public boolean addAll(int index, Collection<? extends T> c) {
            return callListeners(super.addAll(index, c));
        }

        public boolean removeAll(Collection<?> c) {
            return callListeners(super.removeAll(c));
        }

        public boolean retainAll(Collection<?> c) {
            return callListeners(super.retainAll(c));
        }

        public void clear() {
            super.clear();
        }

        public T set(int index, T element) {
            return callListeners(super.set(index, element));
        }

        public void add(int index, T element) {
            super.add(index, element);
        }

        public T remove(int index) {
            return callListeners(super.remove(index));
        }

        @Override
        public List<T> get() {
            return this;
        }

        private <R> R callListeners(R val) {
            for (Consumer<List<T>> consumer : listeners) {
                consumer.accept(get());
            }
            return val;
        }

        private final CopyOnWriteArrayList<Consumer<List<T>>> listeners = new CopyOnWriteArrayList<>();

        @Override
        public Runnable subscribe(Consumer<List<T>> listener) {
            if (!listeners.contains(listener))
                listeners.add(listener);
            listener.accept(get());
            return () -> {
                listeners.remove(listener);
            };
        }

        public Observable<List<T>> readOnly() {
            return new Observable<>() {
                @Override
                public List<T> get() {
                    return List.copyOf(ReactiveArrayList.this.get());
                }

                @Override
                public Runnable subscribe(Consumer<List<T>> l) {
                    return ReactiveArrayList.this.subscribe(mutable -> l.accept(get()));
                }
            };
        }
    }
}