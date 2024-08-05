package net.microfalx.zenith.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessionTest {

    @Test
    void local() {
        Session session = Session.local();
        validateSession(session);
    }

    @Test
    void localWithoutHeadless() {
        Session session = Session.create(Options.create().withHeadless(false));
        validateSession(session);
    }

    @Test
    void get() {
        Session session = Session.get();
        validateSession(session);
    }

    private void validateSession(Session session) {
        assertNotNull(session);
        session.open("https://google.com");
    }
}