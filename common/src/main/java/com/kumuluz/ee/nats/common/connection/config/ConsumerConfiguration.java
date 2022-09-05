package com.kumuluz.ee.nats.common.connection.config;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Class for NATS Consumer settings.
 *
 * @author Matej Bizjak
 */

public class ConsumerConfiguration {

    private String name;

    private DeliverPolicy deliverPolicy;

    private AckPolicy ackPolicy;

    private ReplayPolicy replayPolicy;

    private String description;

    private String deliverSubject;

    private String deliverGroup;

    private String filterSubject;

    private String sampleFrequency;

    private ZonedDateTime startTime;

    private Duration ackWait;

    private Duration idleHeartbeat;

    private Duration maxExpires;

    private Duration inactiveThreshold;

    private Long startSeq;

    private Long maxDeliver;

    private Long rateLimit;

    private Long maxAckPending;

    private Long maxPullWaiting;

    private Long maxBatch;

    private Long maxBytes;

    private Boolean flowControl;

    private Boolean headersOnly;

    private List<Duration> backoff;

    public ConsumerConfiguration() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeliverPolicy getDeliverPolicy() {
        return deliverPolicy;
    }

    public void setDeliverPolicy(DeliverPolicy deliverPolicy) {
        this.deliverPolicy = deliverPolicy;
    }

    public AckPolicy getAckPolicy() {
        return ackPolicy;
    }

    public void setAckPolicy(AckPolicy ackPolicy) {
        this.ackPolicy = ackPolicy;
    }

    public ReplayPolicy getReplayPolicy() {
        return replayPolicy;
    }

    public void setReplayPolicy(ReplayPolicy replayPolicy) {
        this.replayPolicy = replayPolicy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeliverSubject() {
        return deliverSubject;
    }

    public void setDeliverSubject(String deliverSubject) {
        this.deliverSubject = deliverSubject;
    }

    public String getDeliverGroup() {
        return deliverGroup;
    }

    public void setDeliverGroup(String deliverGroup) {
        this.deliverGroup = deliverGroup;
    }

    public String getFilterSubject() {
        return filterSubject;
    }

    public void setFilterSubject(String filterSubject) {
        this.filterSubject = filterSubject;
    }

    public String getSampleFrequency() {
        return sampleFrequency;
    }

    public void setSampleFrequency(String sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getAckWait() {
        return ackWait;
    }

    public void setAckWait(Duration ackWait) {
        this.ackWait = ackWait;
    }

    public Duration getIdleHeartbeat() {
        return idleHeartbeat;
    }

    public void setIdleHeartbeat(Duration idleHeartbeat) {
        this.idleHeartbeat = idleHeartbeat;
    }

    public Duration getMaxExpires() {
        return maxExpires;
    }

    public void setMaxExpires(Duration maxExpires) {
        this.maxExpires = maxExpires;
    }

    public Duration getInactiveThreshold() {
        return inactiveThreshold;
    }

    public void setInactiveThreshold(Duration inactiveThreshold) {
        this.inactiveThreshold = inactiveThreshold;
    }

    public Long getStartSeq() {
        return startSeq;
    }

    public void setStartSeq(Long startSeq) {
        this.startSeq = startSeq;
    }

    public Long getMaxDeliver() {
        return maxDeliver;
    }

    public void setMaxDeliver(Long maxDeliver) {
        this.maxDeliver = maxDeliver;
    }

    public Long getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Long rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Long getMaxAckPending() {
        return maxAckPending;
    }

    public void setMaxAckPending(Long maxAckPending) {
        this.maxAckPending = maxAckPending;
    }

    public Long getMaxPullWaiting() {
        return maxPullWaiting;
    }

    public void setMaxPullWaiting(Long maxPullWaiting) {
        this.maxPullWaiting = maxPullWaiting;
    }

    public Long getMaxBatch() {
        return maxBatch;
    }

    public void setMaxBatch(Long maxBatch) {
        this.maxBatch = maxBatch;
    }

    public Long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(Long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public Boolean getFlowControl() {
        return flowControl;
    }

    public void setFlowControl(Boolean flowControl) {
        this.flowControl = flowControl;
    }

    public Boolean getHeadersOnly() {
        return headersOnly;
    }

    public void setHeadersOnly(Boolean headersOnly) {
        this.headersOnly = headersOnly;
    }

    public List<Duration> getBackoff() {
        return backoff;
    }

    public void setBackoff(List<Duration> backoff) {
        this.backoff = backoff;
    }

}
