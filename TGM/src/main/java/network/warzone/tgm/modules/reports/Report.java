package network.warzone.tgm.modules.reports;

import network.warzone.tgm.util.Strings;

public class Report {
    private String reporter;
    private String reported;
    private String reason;
    private long timestamp;
    private int amount;

    public Report setReporter(String reporter) {
        this.reporter = reporter;
        return this;
    }

    public Report setReported(String reported) {
        this.reported = reported;
        return this;
    }

    public Report setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Report setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Report setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getReporter() {
        return this.reporter;
    }

    public String getReported() {
        return this.reported;
    }

    public String getReason() {
        return this.reason;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getAgo() {
        return Strings.getAgo(this.timestamp);
    }
}
