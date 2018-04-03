package com.github.hdghg.capcha.repository;

import com.github.hdghg.capcha.domain.Tile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for entity {@link Tile}
 */
public interface TileReactiveRepository extends ReactiveCrudRepository<Tile, String> {

    Flux<Tile> findTop5ByOrderByIdDesc();

}
