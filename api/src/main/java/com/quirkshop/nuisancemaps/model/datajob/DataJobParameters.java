package com.quirkshop.nuisancemaps.model.datajob;

import java.util.HashMap;

public interface DataJobParameters {

    public String buildInitURL(DataJob dataJob);

    public String buildNextURL(DataJob dataJob);


    public HashMap<String, Object> getParameters();

    public HashMap<String, Object> getNextParameters();

    public String getUrl();

    public String getNextUrl();
}
