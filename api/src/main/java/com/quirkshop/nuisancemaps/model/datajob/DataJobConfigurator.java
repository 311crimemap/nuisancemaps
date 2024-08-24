package com.quirkshop.nuisancemaps.model.datajob;

public interface DataJobConfigurator {

    public DataJob initialize(DataJob dataJob);

    public DataJob next(DataJob dataJob);
}
