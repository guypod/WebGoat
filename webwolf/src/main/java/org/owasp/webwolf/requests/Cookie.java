package org.owasp.webwolf.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
class Cookie implements Serializable {
    private String cookie;
}
