package com.github.hdghg.capcha.dao;

import com.github.hdghg.capcha.domain.ApplicationUser;
import com.github.hdghg.capcha.repository.ApplicationUserReactiveRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * User details service that gets user details from mongo collection called 'ApplicationUser'.
 * If collection is empty, it provides stub admin user.
 */
@Service
public class MongoReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final ApplicationUserReactiveRepository applicationUserReactiveRepository;

    public MongoReactiveUserDetailsService(
            ApplicationUserReactiveRepository applicationUserReactiveRepository) {
        this.applicationUserReactiveRepository = applicationUserReactiveRepository;
    }

    /**
     * Find user by username
     * @param username Username provided
     * @return Found user of empty.
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return applicationUserReactiveRepository.findByUsername(username)
                .switchIfEmpty(emptyUserCollectionFallback())
                .map(this::convert);
    }

    /**
     * Convert ApplicationUser to UserDetails
     * @param applicationUser ApplicationUser
     * @return UserDetails
     */
    private UserDetails convert(ApplicationUser applicationUser)  {
        int rolesCount = applicationUser.roleList.size();
        return User.withUsername(applicationUser.username)
                .password(applicationUser.password)
                .roles(applicationUser.roleList.toArray(new String[rolesCount]))
                .build();
    }

    /**
     * Fallback method that allows to login as admin if 0 users are in mongo collection.
     * @return Stub user
     */
    private Mono<ApplicationUser> emptyUserCollectionFallback() {
        return applicationUserReactiveRepository.count()
                .filter(count -> 0 == count)
                .map(unbound -> new ApplicationUser("admin", "{noop}password",
                        Collections.singletonList("ADMIN")));
    }
}
