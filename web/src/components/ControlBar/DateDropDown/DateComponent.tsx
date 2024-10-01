import { useState, useMemo, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar } from "@fortawesome/free-regular-svg-icons";

import { debounce } from "lodash";
import DropDown from "../DropDown/index.tsx";
import InputDate from "./InputDate";
import { DateRange, FilterDateDispatcher } from "../../../types/daterange";
import { Log } from "../../../Logger";

interface DateComponentProps {
  filterDate: DateRange;
  filterDateDispatcher: FilterDateDispatcher;
}

export function DateComponent({
  filterDate,
  filterDateDispatcher,
}: DateComponentProps) {
  //intermediate date state to avoid re-renders from top
  const [inputDate, setInputDate] = useState({
    label: filterDate.label,
    startDate: {
      date: filterDate.date.startDate,
      isValid: true,
    },
    endDate: {
      date: filterDate.date.endDate,
      isValid: true,
    },
  });

  const min = new Date();
  min.setDate(min.getDate() - 365);
  const max = new Date();
  max.setDate(max.getDate() - 1);

  //en-CA? need YYYY-MM-DD format string for <input>
  const startMinDate = min.toLocaleDateString("en-CA");
  const startMaxDate = max.toLocaleDateString("en-CA");
  const endMinDate = min.toLocaleDateString("en-CA");
  const endMaxDate = max.toLocaleDateString("en-CA");

  const debounceFilterDateDispatcher = useMemo(() => {
    return debounce(filterDateDispatcher, 350);
  }, []);

  //only request on valid date ranges
  useEffect(() => {
    if (inputDate.startDate.isValid && inputDate.endDate.isValid) {
      debounceFilterDateDispatcher({
        type: "setDate",
        value: {
          startDate: inputDate.startDate.date,
          endDate: inputDate.endDate.date,
        },
      });
    }
  }, [inputDate]);

  //if date presets are changed, reload to display values in controls
  useEffect(() => {
    setInputDate({
      label: filterDate.label,
      startDate: {
        date: filterDate.date.startDate,
        isValid: true,
      },
      endDate: {
        date: filterDate.date.endDate,
        isValid: true,
      },
    });
  }, [filterDate.date.startDate, filterDate.date.endDate]);

  const dateStyle = {
    fontWeight: "normal",
  };
  Log.log({ msg: "[DateComponent]", params: { filterDate }, ...Log.data });
  return (
    <DropDown>
      <div>
        <span className="mr-2">
          <FontAwesomeIcon icon={faCalendar} />
        </span>
        <span style={dateStyle}>{inputDate.label}</span>
      </div>

      <div className="flex flex-col gap-6">
        {/* Preset */}
        <div className="flex flex-col gap-2">
          <label>
            {" "}
            <strong>Date Range Preset</strong>{" "}
          </label>
          <select
            id="presetDate"
            name="presetDate"
            className="select select-bordered w-full"
            defaultValue=""
            onChange={(e) =>
              debounceFilterDateDispatcher({
                type: "calcDate",
                value: e.target.value,
              })
            }
          >
            <option disabled value="">
              Select timeframe from today
            </option>
            <option value="1 Day"> 1 Day</option>
            <option value="3 Days"> 3 Days</option>
            <option value="1 Week"> 1 Week</option>
            <option value="2 Weeks"> 2 Weeks</option>
            <option value="1 Month"> 1 Month </option>
            <option value="2 Months"> 2 Months </option>
            <option value="3 Months"> 3 Months </option>
            <option value="6 Months"> 6 Months </option>
          </select>
        </div>

        {/*
         * Loading Screen Divider
         *
         * NB: triggering select disabled causes dropdown to lose focus and close
         * which is undesirable. So we just have an indicator.
         *
         */}

        <div className="divider">
          {filterDate.isBusy ? (
            <>
              <span className="loading loading-spinner loading-lg"></span>
            </>
          ) : (
            <>OR</>
          )}
        </div>

        {/* Calendars */}
        <div className="flex flex-col gap-4">
          <strong>Custom Date Range</strong>
          <div className="flex flex-col gap-2">
            <label htmlFor="start">Start</label>
            <InputDate
              id="startDate"
              name="startDate"
              minDate={startMinDate}
              maxDate={startMaxDate}
              inputDate={inputDate}
              setInputDate={setInputDate}
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="end">End</label>
            <InputDate
              id="endDate"
              name="endDate"
              minDate={endMinDate}
              maxDate={endMaxDate}
              inputDate={inputDate}
              setInputDate={setInputDate}
            />
          </div>
        </div>
      </div>
    </DropDown>
  );
}
