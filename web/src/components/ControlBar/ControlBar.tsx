import { useState, useEffect, useMemo, useRef } from "react";
import { Search } from "./Search";
import { DateComponent } from "./DateComponent";
import { DropDownFilter } from "./DropDownFilter";

export default function ControlBar({
  activeCategories,
  activeCategoriesDispatcher,
  filterDate,
  filterDateDispatcher,
}) {
  if (activeCategories.length == 0) return null;

  return (
    <div className="navbar bg-base-100">
      <Search />
      <DateComponent
        filterDate={filterDate}
        filterDateDispatcher={filterDateDispatcher}
      />
      <DropDownFilter
        type="crime"
        activeCategories={activeCategories}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
      />
      <DropDownFilter
        type="311"
        activeCategories={activeCategories}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
      />
      {/*
                        <Sidebar
                            map={map}
                            activeReportNum={activeReportNum}
                            setActiveReportNum={setActiveReportNum}
                            activeCategories={activeCategories}
                            activeCategoriesDispatcher={activeCategoriesDispatcher}
                            filterDate={filterDate}
                            filterDateDispatcher={filterDateDispatcher}
                        />
                */}
    </div>
  );
}
