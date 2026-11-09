package com.extantape.signalbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {
    @Test
    void readsTheConsoleAndLogFileForms() {
        Reader r = new Reader();
        r.expect("abc"); r.expect("def");
        assertTrue(r.accept("[04:36:31 INFO]: ea-result abc {\"id\":\"abc\",\"ok\":true}").isPresent());
        assertTrue(r.accept("[04:36:31] [Server thread/INFO]: ea-result def {\"id\":\"def\",\"ok\":true}").isPresent());
    }

    @Test
    void aPlayerTypingAnAnswerInChatIsNeverRead() {
        Reader r = new Reader();
        r.expect("abc");
        assertTrue(r.accept("[04:36:31 INFO]: <Mallory> ea-result abc {\"id\":\"abc\",\"ok\":true}").isEmpty());
        assertTrue(r.accept("[04:36:31 INFO]: [Usher] ea-result abc {\"id\":\"abc\",\"ok\":true}").isEmpty(), "a logger's tag doesn't count either");
        assertTrue(r.accept("[04:36:31 INFO]: ea-result abc {\"id\":\"abc\",\"ok\":true}").isPresent(), "control: the genuine line still counts");
    }

    @Test
    void onlyTheFirstAnswerAndOnlyForIdsWeSent() {
        Reader r = new Reader();
        r.expect("abc");
        assertTrue(r.accept("[t INFO]: ea-result abc {\"ok\":true}").isPresent());
        assertTrue(r.accept("[t INFO]: ea-result abc {\"ok\":false}").isEmpty(), "a second answer is ignored");
        assertTrue(r.accept("[t INFO]: ea-result zzz {\"ok\":true}").isEmpty(), "an id we never sent is ignored");
    }
}
