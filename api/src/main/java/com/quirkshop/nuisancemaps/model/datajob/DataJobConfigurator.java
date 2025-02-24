package com.quirkshop.nuisancemaps.model.datajob;

public interface DataJobConfigurator {
    /**
     * Initializes a DataJob with default parameters and constructs the data job
     * URL.
     *
     * @param dataJob the DataJob to initialize; if null, returns null.
     * @return the initialized DataJob with updated parameters and URL, or null if
     *         the input was null.
     */
    public DataJob initialize(DataJob dataJob);

    /**
     * Updates the parameters of the DataJob for the next execution cycle; this
     * could include adjusting parameter start dates, or incrementing offset
     * counts when creating next URL sequence.
     *
     * @param dataJob the DataJob to update; if null, returns null.
     * @return the updated DataJob with new parameters and URL, or null if no
     *         further updates are needed.
     */
    public DataJob next(DataJob dataJob);
}
