package com.quirkshop.nuisancemaps.service.dataprocess;

import com.quirkshop.nuisancemaps.model.DataJob;

public interface DataProcessStrategy {

    public String fetch(DataJob datajob);
    public void process();
}
