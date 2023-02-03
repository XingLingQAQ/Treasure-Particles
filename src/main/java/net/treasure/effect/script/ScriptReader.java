package net.treasure.effect.script;

import net.treasure.common.Patterns;
import net.treasure.effect.Effect;
import net.treasure.effect.exception.ReaderException;
import net.treasure.util.UnsafeConsumer;
import net.treasure.util.logging.ComponentLogger;

import java.util.HashMap;
import java.util.Locale;

public abstract class ScriptReader<C extends ReaderContext<?>, T> {

    private final HashMap<String, UnsafeConsumer<C>> validArguments = new HashMap<>();

    public C createContext(Effect effect, String type, String line) {
        return null;
    }

    public boolean validate(C context) throws ReaderException {
        return true;
    }

    public void addValidArgument(UnsafeConsumer<C> consumer, String... aliases) {
        if (aliases == null || aliases.length == 0) return;
        for (var key : aliases)
            validArguments.put(key, consumer);
    }

    public boolean isValidArgument(String key) {
        return validArguments.containsKey(key);
    }

    public T read(Effect effect, String type, String line) throws ReaderException {
        var context = createContext(effect, type, line);
        var matcher = Patterns.SCRIPT.matcher(line);

        while (matcher.find()) {
            String key = matcher.group("type");
            String value = matcher.group("value");
            int start = matcher.start(), end = matcher.end();
            if (key == null || value == null)
                continue;

            key = key.toLowerCase(Locale.ENGLISH);

            context.start(start)
                    .end(end)
                    .key(key)
                    .value(value);

            if (!isValidArgument(key)) {
                error(context, "Unexpected argument: " + key);
                continue;
            }

            var argument = validArguments.get(key);
            try {
                argument.accept(context);
            } catch (Exception e) {
                error(context, "Unexpected argument value: " + e.getMessage());
            }
        }

        if (!validate(context)) return null;

        //noinspection unchecked
        return (T) context.script();
    }

    public void error(C context, String... messages) {
        ComponentLogger.error(context, messages);
    }

    public void error(Effect effect, String type, String line, String... messages) throws ReaderException {
        ComponentLogger.error(effect, type, line, messages);
        throw new ReaderException();
    }

    public void error(Effect effect, String type, String line, int start, int end, String... messages) throws ReaderException {
        ComponentLogger.error(effect, type, line, start, end, messages);
        throw new ReaderException();
    }
}