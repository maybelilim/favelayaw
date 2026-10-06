package net.favela.yaw.impl.event;

import net.favela.yaw.impl.util.log.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Events {

    private static final Map<Class<?>, CopyOnWriteArrayList<Handler<?>>> HANDLERS = new ConcurrentHashMap<>();

    public record Handler<T extends Event>(
            Class<T> type,
            Priority priority,
            boolean receiveCancelled,
            Consumer<T> action
    ) {
    }

    private Events() {

    }

    public static <T extends Event> Handler<T> handler(Class<T> type, Consumer<T> action) {
        return new Handler<>(type, Priority.NORMAL, false, action);
    }

    public static <T extends Event> Handler<T> on(Class<T> type, Consumer<T> action) {
        Handler<T> handler = handler(type, action);
        register(handler);
        return handler;
    }

    public static <T extends Event> Handler<T> on(Class<T> type, Priority priority, Consumer<T> action) {
        return on(type, priority, false, action);
    }

    public static <T extends Event> Handler<T> on(Class<T> type, Priority priority,
                                                  boolean receiveCancelled, Consumer<T> action) {
        Handler<T> handler = new Handler<>(type, priority, receiveCancelled, action);
        register(handler);
        return handler;
    }

    public static void register(Handler<?> handler) {
        CopyOnWriteArrayList<Handler<?>> list =
                HANDLERS.computeIfAbsent(handler.type(), k -> new CopyOnWriteArrayList<>());

        synchronized (list) {
            int i = 0;
            for (Handler<?> existing : list) {
                if (handler.priority().ordinal() > existing.priority().ordinal()) break;
                i++;
            }
            list.add(i, handler);
        }
    }

    public static void off(Handler<?> handler) {
        CopyOnWriteArrayList<Handler<?>> list = HANDLERS.get(handler.type());
        if (list == null) return;
        list.remove(handler);
        if (list.isEmpty()) HANDLERS.remove(handler.type(), list);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> T post(T event) {
        CopyOnWriteArrayList<Handler<?>> list = HANDLERS.get(event.getClass());
        if (list == null || list.isEmpty()) return event;

        boolean cancellable = event instanceof Cancellable;
        for (Handler<?> handler : list) {
            if (cancellable && ((Cancellable) event).isCancelled() && !handler.receiveCancelled()) {
                continue;
            }
            try {
                ((Handler<T>) handler).action().accept(event);
            } catch (Throwable t) {
                Log.error("Handler threw for {}", event.getClass().getSimpleName(), t);
            }
        }
        return event;
    }
}