import { useState, useMemo, useEffect } from "react";

import {debounce} from "lodash";
import DropDown from "../DropDown/index.tsx";
import InputDate from "./InputDate";
import { DateRange, FilterDateDispatcher } from "../../../types/daterange";

interface DateComponentProps {
    filterDate: DateRange;
    filterDateDispatcher: FilterDateDispatcher;
}

export function DateComponent({ filterDate, filterDateDispatcher }: DateComponentProps) {

  //intermediate date state to avoid re-renders from top
  const [inputDate, setInputDate] = useState({
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

  console.log("[DateComponent]", filterDate);
  return (
    <DropDown>
      <div>Date</div>

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
            <option value="1"> 1 day</option>
            <option value="3"> 3 days</option>
            <option value="7"> 1 week</option>
            <option value="14"> 2 weeks</option>
            <option value="1 month"> 1 month </option>
            <option value="2 month"> 2 months </option>
            <option value="3 month"> 3 months </option>
            <option value="6 month"> 6 months </option>
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
