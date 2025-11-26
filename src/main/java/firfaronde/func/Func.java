package firfaronde.func;

import discord4j.core.event.domain.message.MessageCreateEvent;

@FunctionalInterface
public interface Func<T, A> {
    void accept(T event, A args);
}
