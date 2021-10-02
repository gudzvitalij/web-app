package org.example.app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.example.framework.attribute.RequestAttributes;

import java.util.regex.Matcher;

public class CardHelper {
    private CardHelper(){}

    public static long getCardId(HttpServletRequest req){
        return Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                .group("cardId"));
    }
}
