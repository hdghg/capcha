package com.github.hdghg.trapcha.repository;

import com.github.hdghg.trapcha.domain.SessionMeta;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SessionMetaReactiveRepository extends ReactiveCrudRepository<SessionMeta, String> {

    Mono<SessionMeta> findByName(String name);
}
