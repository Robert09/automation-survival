package me.formercanuck.automationsurvival.common.eventsystem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {

    private final Map<Class<?>, List<RegisteredHandler>> handlers = new HashMap<>();

    private EventBus() {
        // Private constructor to prevent instantiation
    }

    private static final EventBus instance = new EventBus();

    public static EventBus get() {
        return instance;
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventType)) continue;

            method.setAccessible(true);
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new RegisteredHandler(listener, method));
        }
    }

    public void unregister(Object listener) {
        for (List<RegisteredHandler> list : handlers.values()) {
            list.removeIf(rh -> rh.listener.equals(listener));
        }
    }

    public void call(Event event) {
        Class<?> eventClass = event.getClass();
        List<RegisteredHandler> originalList = handlers.get(eventClass);
        if (originalList == null) return;

        // Defensive copy to avoid modification issues
        List<RegisteredHandler> list = new ArrayList<>(originalList);

        for (RegisteredHandler rh : list) {
            try {
                rh.method.invoke(rh.listener, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class RegisteredHandler {
        final Object listener;
        final Method method;

        RegisteredHandler(Object listener, Method method) {
            this.listener = listener;
            this.method = method;
        }
    }
}
