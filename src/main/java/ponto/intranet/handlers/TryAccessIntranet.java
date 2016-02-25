package ponto.intranet.handlers;

import java.io.IOException;
import java.util.Optional;

/**
 * @author gabriel.schmoeller
 */
public interface TryAccessIntranet<I, O> {

    O tryDo(Optional<I> input) throws IOException;
}
