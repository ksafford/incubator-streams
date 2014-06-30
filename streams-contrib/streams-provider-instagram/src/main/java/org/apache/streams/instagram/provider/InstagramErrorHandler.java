package org.apache.streams.instagram.provider;


import org.jinstagram.Instagram;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstagramErrorHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramErrorHandler.class);

    protected static final long initial_backoff = 1000;
    protected static long backoff = initial_backoff;

    public static int handleInstagramError(Instagram instagram, Exception exception) {
        if (exception instanceof InstagramException) {
            InstagramException e = (InstagramException) exception;
            if (e.getAPILimitStatus() == 0) {
                LOGGER.warn("Rate Limit Exceeded");
                try {
                    Thread.sleep(backoff *= 2);
                } catch (InterruptedException e1) {
                }
                return 1;
            }
//            else if(e.isCausedByNetworkIssue())
//            {
//                LOGGER.info("Instagram Network Issues Detected. Backing off...");
//                LOGGER.info("{} - {}", e.getExceptionCode(), e.getLocalizedMessage());
//                try {
//                    Thread.sleep(backoff *= 2);
//                } catch (InterruptedException e1) {}
//                return 1;
//            }
//            else if(e.isErrorMessageAvailable())
//            {
//                if(e.getMessage().toLowerCase().contains("does not exist"))
//                {
//                    LOGGER.warn("User does not exist...");
//                    return 100;
//                }
//                else
//                {
//                    return 1;
//                }
//            }
//            else
//            {
//                if(e.getExceptionCode().equals("ced778ef-0c669ac0"))
//                {
//                    // This is a known weird issue, not exactly sure the cause, but you'll never be able to get the data.
//                    return 5;
//                }
            else {
                LOGGER.warn("Unknown Instagram Exception...");
                LOGGER.warn("  Account: {}", instagram);
                LOGGER.warn("   Cause: {}", e.getCause());
                LOGGER.warn("     StackTrace: {}", e.getStackTrace());
                LOGGER.warn("  Message: {}", e.getLocalizedMessage());
                return 1;
            }
        } else if (exception instanceof RuntimeException) {
            LOGGER.warn("InstagramGrabber: Unknown Runtime Error", exception.getMessage());
            return 1;
        } else {
            LOGGER.info("Completely Unknown Exception: {}", exception);
            return 1;
        }
    }

}