package io.github.raulbolivarnavas.supportlogging.model;

import io.github.raulbolivarnavas.supportlogging.dto.SupportLogData;
import org.springframework.stereotype.Component;
import reactor.util.context.ContextView;

/**
 * Spring component that provides access to SupportLogData stored in the Reactor context.
 * This component retrieves support log data from the reactive context using a predefined key.
 * If the data is not present in the context, it returns a new default instance.
 */
@Component
@SuppressWarnings("javadoc")
public class SupportLogContext {

    /**
     * The key used to store and retrieve SupportLogData in the Reactor context.
     */
    public static final String KEY = SupportLogData.class.getName();

    /**
     * Retrieves the SupportLogData from the provided Reactor context view.
     * If the data is not present, returns a new instance with default values.
     *
     * @param contextView The Reactor context view from which to retrieve the SupportLogData.
     * @return The SupportLogData retrieved from the context, or a new default instance if not present.
     */
    public SupportLogData get(ContextView contextView) {
        return contextView.getOrDefault(KEY, SupportLogData.builder().build());
    }
}