package com.github.hdghg.capcha.repository;

import com.github.hdghg.capcha.domain.SessionMeta;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for collection 'sessionMeta'
 */
public interface SessionMetaReactiveRepository extends ReactiveCrudRepository<SessionMeta, String> {

    /**
     * Returns {@link SessionMeta} by it's {@link SessionMeta#guid} value
     * @param guid Guid of session
     * @return Entire SessionMeta.
     */
    Mono<SessionMeta> findByGuid(String guid);
}
