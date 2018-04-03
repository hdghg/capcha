package com.github.hdghg.capcha.dao;

import com.github.hdghg.capcha.domain.Tile;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * TileDao used when {@link com.github.hdghg.capcha.repository.TileReactiveRepository} cannot
 * be used
 */
@Repository
public class TileDao {

    private final ReactiveMongoTemplate mongoTemplate;

    public TileDao(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Returns 9 random images
     * @return Flux of 9 random images
     */
    public Flux<Tile> findSampleTiles() {
        Aggregation aggregation = newAggregation(sample(9));
        return mongoTemplate.aggregate(aggregation, Tile.class, Tile.class);
    }
}
