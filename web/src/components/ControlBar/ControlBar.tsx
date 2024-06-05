import { useState, useEffect, useMemo, useRef } from "react";
import { Search } from "./Search";
import { DateComponent } from "./DateDropDown/DateComponent";
import { DropDownFilter } from "./CategoryDropDown/CategoryDropDownComponent";

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
    </div>
  );
}
