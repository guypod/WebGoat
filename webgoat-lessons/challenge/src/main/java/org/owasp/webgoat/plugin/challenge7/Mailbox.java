package org.owasp.webgoat.plugin.challenge7;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nbaars
 * @since 8/17/17.
 */

public class Mailbox {

    private List<Email> mails = new ArrayList();

    public void addMail(Email mail) {
        mails.add(mail);
    }

    public List<Email> getMails() {
        return this.mails;
    }

    @Builder
    @Data
    public static class Email {

        private LocalDateTime time;
        private String contents;
        private String sender;
        private String title;
    }
}
