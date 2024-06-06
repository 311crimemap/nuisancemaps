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
      <div className="flex flex-col sm:flex-row w-full">
        <div className="w-full sm:w-auto">
          <Search />
        </div>

        <div className="flex flex-row w-full sm:w-auto -ml-4 sm:ml-2">
          <DateComponent
            filterDate={filterDate}
            filterDateDispatcher={filterDateDispatcher}
          />

          {/* TODO: merge crime/311 filters in mobile view */}
          <div className="flex sm:hidden">
            <DropDownFilter
              type="crime"
              activeCategories={activeCategories}
              activeCategoriesDispatcher={activeCategoriesDispatcher}
            />
          </div>

          <div className="hidden sm:flex">
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
          <div className="flex items-center">
            <input
              type="checkbox"
              className="toggle toggle-xs sm:toggle-sm ml-2"
            />
            <span className="ml-2">Heatmap</span>
          </div>
        </div>
      </div>
    </div>
  );
}
