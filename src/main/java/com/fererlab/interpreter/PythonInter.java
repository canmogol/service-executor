package com.fererlab.interpreter;


import com.fererlab.log.FLogger;
import com.fererlab.util.PerfCounter;
import org.python.util.PythonInterpreter;

import java.util.logging.Logger;

public class PythonInter {

    private static final Logger log = FLogger.getLogger(PythonInter.class.getSimpleName());

    private PythonInterpreter pythonInterpreter = null;

    private static class Instance {
        private static final PythonInter INSTANCE = new PythonInter();
    }

    private PythonInter() {
        log.info("will create python interpreter");
        long time = System.currentTimeMillis();
        pythonInterpreter = new PythonInterpreter();
        long creationTime = System.currentTimeMillis() - time;
        PerfCounter.add(PerfCounter.PYTHON_INTERPRETER_CREATED, creationTime);
        log.info("python interpreter created: " + creationTime);
    }

    public static PythonInter getInstance() {
        return Instance.INSTANCE;
    }

    public PythonInterpreter getPythonInterpreter() {
        return pythonInterpreter;
    }
}
