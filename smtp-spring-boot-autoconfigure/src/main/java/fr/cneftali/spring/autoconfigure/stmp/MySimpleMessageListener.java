package fr.cneftali.spring.autoconfigure.stmp;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class MySimpleMessageListener implements SimpleMessageListener {

    private static final Log LOGGER = LogFactory.getLog(SubEthaSMTPAutoConfiguration.class);

    private List<SimpleMessage> messages = new LinkedList<>();

    @Override
    public boolean accept(final String from, final String recipient) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Accepting mail from " + from + " to " + recipient);
        }
        return true;
    }

    @Override
    public void deliver(final String from, final String recipient, final InputStream data) throws TooMuchDataException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Delivering mail from " + from + " to " + recipient);
        }
        final String message = CharStreams.toString(new InputStreamReader(data, Charsets.UTF_8));
        this.messages.add(new SimpleMessage(from, recipient, message));
    }

    public List<SimpleMessage> getMessages() {
        return messages;
    }
}
