import { useState, useEffect, useMemo, useRef } from "react";
import { Search } from "./Search";
import { DateComponent } from "./DateDropDown/DateComponent";
import { DropDownFilter } from "./CategoryDropDown/CategoryDropDownComponent";
import { ToggleComponent } from "./Toggle/ToggleComponent";
import MapInfo from "./MapInfo";
export default function ControlBar({
  map,
  mapController,
  DATASOURCES,
  setActiveFeatureList,
  activeCategories,
  activeCategoriesDispatcher,
  filterDate,
  filterDateDispatcher,
  dataCrimes,
  data311s,
  isDataLoading,
}) {
  if (activeCategories.length == 0) return null;

  return (
    <div className="navbar bg-base-100 p-4 pb-2 sm:py-2 border-b">
      <div className="flex flex-col sm:flex-row w-full flex-1">
        <div className="w-full sm:w-auto mb-2 sm:mb-0">
          <Search mapController={mapController} />
        </div>

        {/* Dropdowns sm+ */}
        <div className="flex flex-row w-full sm:w-auto -ml-4 sm:ml-2">
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

          <div className="flex flex-col justify-center items-center">
            <ToggleComponent
              map={map}
              setActiveFeatureList={setActiveFeatureList}
            />
          </div>
        </div>
      </div>

      <div className="flex-none hidden md:flex">
        <MapInfo
          map={map}
          dataCrimes={dataCrimes}
          data311s={data311s}
          isDataLoading={isDataLoading}
        />
      </div>
    </div>
  );
}
