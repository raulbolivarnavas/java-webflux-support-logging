package io.github.raulbolivarnavas.supportlogging.masking;

/**
 * Applies configured masking rules to log payloads.
 */
public interface DataMasker {
    /**
     * Masks the supplied value when masking is enabled.
     *
     * @param value the value to mask
     * @return the masked value, or the original value when masking is disabled
     */
    Object mask(Object value);
}
