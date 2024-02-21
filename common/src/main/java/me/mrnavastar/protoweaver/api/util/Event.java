package me.mrnavastar.protoweaver.api.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.function.Function;

public class Event<T> {

    @Getter
    public static class Cancelable {
        private boolean canceled = false;

        /**
         * Cancels the current event from happening as well as all future handlers of the current event.
         */
        public void cancel() {
            canceled = true;
        }
    }

    private final ArrayList<T> handlers = new ArrayList<>();
    private final Function<ArrayList<T>, T> invokerFactory;

    public Event(Function<ArrayList<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
    }

    public void register(T handler) {
        handlers.add(handler);
    }

    public T getInvoker() {
        return invokerFactory.apply(handlers);
    }
}