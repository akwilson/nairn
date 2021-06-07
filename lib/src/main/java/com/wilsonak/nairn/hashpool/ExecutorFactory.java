package com.wilsonak.nairn.hashpool;

import java.util.concurrent.Executor;

public interface ExecutorFactory {
    Executor getExecutor(String id);
}
