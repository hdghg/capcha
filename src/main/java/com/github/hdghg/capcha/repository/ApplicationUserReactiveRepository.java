package com.github.hdghg.capcha.repository;

import com.github.hdghg.capcha.domain.ApplicationUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for {@link ApplicationUser}
 */
public interface ApplicationUserReactiveRepository extends ReactiveCrudRepository<ApplicationUser, String> {

    Mono<ApplicationUser> findByUsername(String username);
}
