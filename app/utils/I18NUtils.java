package utils;

import java.util.Arrays;

import play.i18n.Messages;

public class I18NUtils {
    public static String renderErrorMessage(Messages messages, String errorMessage) {
        if (errorMessage.contains("||")) {
            String[] tokens = errorMessage.split("\\|\\|");
            return messages.at(tokens[0], (Object[]) Arrays.copyOfRange(tokens, 1, tokens.length));
        }
        return errorMessage;
    }
}
