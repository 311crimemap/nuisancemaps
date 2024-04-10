package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;

public interface IDataEntity {

    public Integer getId();

    public void setId(Integer id);

    public String getReportNum();

    public Category getOrgCategory();

    public void setReportNum(String reportNum);

    public void setReportCategory(String category);

    public void setDescription(String description);

    public void setLocation(String location);

    public void setOrgCategory(Category orgCategory);

    public void setLatitude(Double latitude);

    public void setLongitude(Double longitude);

    public void setPoint(Point point);

    public void setReportedAt(LocalDateTime reportedAt);

    public void setUpdatedAt(LocalDateTime updatedAt);

}
