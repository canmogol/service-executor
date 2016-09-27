package com.fererlab.language.ruby;


import com.fererlab.log.FLogger;
import com.fererlab.util.PerfCounter;
import org.jruby.embed.ScriptingContainer;

import java.util.logging.Logger;

public class RubyInter {

    private static final Logger log = FLogger.getLogger(RubyInter.class.getSimpleName());

    private ScriptingContainer rubyInterpreter = null;

    private static class Instance {
        private static final RubyInter INSTANCE = new RubyInter();
    }

    private RubyInter() {
        log.info("will create ruby interpreter");
        long time = System.currentTimeMillis();
        rubyInterpreter = new ScriptingContainer();
        long creationTime = System.currentTimeMillis() - time;
        PerfCounter.add(PerfCounter.RUBY_INTERPRETER_CREATED, creationTime);
        log.info("ruby interpreter created: " + creationTime + " milli seconds");
    }

    public static RubyInter getInstance() {
        return Instance.INSTANCE;
    }

    public ScriptingContainer getRubyInterpreter() {
        return rubyInterpreter;
    }
}
