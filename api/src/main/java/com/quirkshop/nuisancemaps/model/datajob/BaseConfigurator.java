package com.quirkshop.nuisancemaps.model.datajob;

public class BaseConfigurator implements DataJobConfigurator {

    public DataJob initialize(DataJob dataJob) {
        if (dataJob == null)
            return null;

        String url = dataJob.getSourceURL();
        dataJob.setUrl(url);
        return dataJob;
    }

    public DataJob next(DataJob dataJob) {
        return null;
    }


}
