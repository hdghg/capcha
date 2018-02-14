package com.github.hdghg.trapcha.repository;

import com.github.hdghg.trapcha.domain.Tile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Reactive repository for entity {@link Tile}
 */
public interface TileReactiveRepository extends ReactiveCrudRepository<Tile, String> {

}
