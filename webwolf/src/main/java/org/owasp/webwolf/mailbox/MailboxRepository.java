package org.owasp.webwolf.mailbox;

import com.hazelcast.core.HazelcastInstance;
import lombok.AllArgsConstructor;
import org.owasp.webgoat.plugin.challenge7.Mailbox;
import org.springframework.stereotype.Component;

/**
 * @author nbaars
 * @since 8/17/17.
 */
@Component
@AllArgsConstructor
public class MailboxRepository {

    private final HazelcastInstance hazelcastInstance;

    public Mailbox mailbox(String username) {
        return (Mailbox) hazelcastInstance.getMap("usersMail").getOrDefault(username, new Mailbox());
    }


}
