package com.quirkshop.nuisancemaps.service;

import com.quirkshop.nuisancemaps.model.DataJob;

interface DataJobRequestService {

    public String fetchJSON(DataJob datajob);

    public int createData();
}
