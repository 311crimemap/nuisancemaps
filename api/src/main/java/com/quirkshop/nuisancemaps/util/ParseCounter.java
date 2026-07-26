package com.quirkshop.nuisancemaps.util;

public class ParseCounter {
    int numFetched = 0;
    int numSkipped = 0;
    int numMissing = 0;
    int numExceededThreshold = 0;
    int numBuilt = 0;
    int numErrors = 0;
    int numRowErrors = 0;

    int numReplaced = 0;
    int numProcessed = 0;
    int numDuplicates = 0;
    int numInserted = 0;
    int numUnchangedDuplicates = 0;

    int numBatch = 0;

    public ParseCounter() {
    }

    public void numRowErrorsIncrement() {
        this.numRowErrors++;
    }

    public void numFetchedIncrement() {
        this.numFetched++;
    }

    public void numSkippedIncrement() {
        this.numSkipped++;
    }

    public void numMissingIncrement() {
        this.numMissing++;
    }

    public void numExceededThresholdIncrement() {
        this.numExceededThreshold++;
    }

    public void numBuiltIncrement() {
        this.numBuilt++;
    }

    public void numErrorsIncrement() {
        this.numErrors++;
    }

    public void setNumReplace(int numReplaced) {
        this.numReplaced = numReplaced;
    }

    public void setNumProcessed(int numProcessed) {
        this.numProcessed = numProcessed;
    }

    public void setNumDuplicates(int numDuplicates) {
        this.numDuplicates = numDuplicates;
    }

    public void setNumInserted(int numInserted) {
        this.numInserted = numInserted;
    }

    public void setNumUnchangedDuplicates(int numUnchangedDuplicates) {
        this.numUnchangedDuplicates = numUnchangedDuplicates;
    }

    public int getNumFetched() {
        return numFetched;
    }

    public int getNumSkipped() {
        return numSkipped;
    }

    public int getNumBuilt() {
        return numBuilt;
    }

    public int getNumErrors() {
        return numErrors;
    }

    public int getNumReplaced() {
        return numReplaced;
    }

    public int getNumProcessed() {
        return numProcessed;
    }

    public int getNumDuplicates() {
        return numDuplicates;
    }

    public int getNumInserted() {
        return numInserted;
    }

    public int getNumUnchangedDuplicates() {
        return numUnchangedDuplicates;
    }

    public int getNumMissing() {
        return numMissing;
    }

    public int getNumRowErrors() {
        return numRowErrors;
    }

    public void setNumFetched(int numFetched) {
        this.numFetched = numFetched;
    }

    public void setNumSkipped(int numSkipped) {
        this.numSkipped = numSkipped;
    }

    public void setNumMissing(int numMissing) {
        this.numMissing = numMissing;
    }

    public void setNumBuilt(int numBuilt) {
        this.numBuilt = numBuilt;
    }

    public void setNumErrors(int numErrors) {
        this.numErrors = numErrors;
    }

    public void setNumRowErrors(int numRowErrors) {
        this.numRowErrors = numRowErrors;
    }

    public void setNumReplaced(int numReplaced) {
        this.numReplaced = numReplaced;
    }

    public int getNumBatch() {
        return numBatch;
    }

    public void setNumBatch(int numBatch) {
        this.numBatch = numBatch;
    }

    public int getNumExceededThreshold() {
        return numExceededThreshold;
    }

    public void setNumExceededThreshold(int numExceededThreshold) {
        this.numExceededThreshold = numExceededThreshold;
    }
}
