package org.owasp.webwolf.mailbox;

import lombok.AllArgsConstructor;
import org.owasp.webgoat.plugin.challenge7.Mailbox;
import org.owasp.webwolf.WebGoatUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author nbaars
 * @since 8/17/17.
 */
@RestController
@AllArgsConstructor
public class MailboxController {

    private final MailboxRepository mailboxRepository;

    @GetMapping(value = "/mail")
    public ModelAndView mail() {
        WebGoatUser user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView modelAndView = new ModelAndView();
        Mailbox mailbox = mailboxRepository.mailbox(user.getUsername());
        modelAndView.addObject("total", mailbox.size());
        modelAndView.addObject("emails", mailbox.getMails());
        modelAndView.setViewName("mailbox");
        return modelAndView;
    }

}
