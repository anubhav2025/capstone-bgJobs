package com.capstone.bgJobs.model;

import java.util.List;

public class SearchFindingsResult {
    private List<Finding> findings;
    private long total;


    
    public SearchFindingsResult(List<Finding> findings, long total) {
        this.findings = findings;
        this.total = total;
    }
    public List<Finding> getFindings() {
        return findings;
    }
    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }
    public long getTotal() {
        return total;
    }
    public void setTotal(long total) {
        this.total = total;
    }

    // constructor, getters, setters
}
