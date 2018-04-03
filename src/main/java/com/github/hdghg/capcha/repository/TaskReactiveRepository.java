package com.github.hdghg.capcha.repository;

import com.github.hdghg.capcha.domain.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Generated tasks stored in this repository. When validating captcha, task found by
 * id and set of answers provided by user compared to set of stored answers.
 */
public interface TaskReactiveRepository extends ReactiveCrudRepository<Task, String> {
}
