package fr.cneftali.spring.autoconfigure.stmp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class SimpleMessage {
    private final String messageData;
    private final String envelopeSender;
    private final String envelopeReceiver;
}
