package org.openrepose.core.services.reporting.destinations;

public interface DestinationInfo {

    void incrementRequestCount();
    void incrementResponseCount();
    void incrementStatusCodeCount(int statusCode, long time);
    void accumulateResponseTime(long responseTime);    
    String getDestinationId();
    long getTotalRequests();
    long getTotalStatusCode(int statusCode);
    long getTotalResponseTime(int statusCode);
    double getAverageResponseTime();
    double getThroughput();

    DestinationInfo copy();
}
