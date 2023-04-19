package com.mojang.realmsclient.exception;

import org.slf4j.Logger;

public class RealmsDefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Logger logger;

    public RealmsDefaultUncaughtExceptionHandler(Logger pLogger)
    {
        this.logger = pLogger;
    }

    public void uncaughtException(Thread pThread, Throwable pThrowable)
    {
        this.logger.error("Caught previously unhandled exception", pThrowable);
    }
}
