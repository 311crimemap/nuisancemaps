import { useState, useEffect, useReducer } from "react";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";
import dateFilterReducer from "./DateFilterReducer";

export default function FiltersView({
  map,
  activeCategories,
  activeCategoriesDispatcher,
}) {
  //const [startDate, setStartDate] = useState(startMaxDate);
  // const [endDate, setEndDate] = useState(endMaxDate);

  const min = new Date();
  min.setDate(min.getDate() - 365);
    const max = new Date();

  //en-CA? need YYYY-MM-DD format string for <input>
  const startMinDate = min.toLocaleDateString("en-CA");
  const startMaxDate = max.toLocaleDateString("en-CA");
  const endMinDate = min.toLocaleDateString("en-CA");
    const endMaxDate = max.toLocaleDateString("en-CA");

  const [filterDate, filterDateDispatcher] = useReducer(dateFilterReducer, {
    date: { startDate: startMaxDate, endDate: endMaxDate },
  });

  const parentCrime = activeCategories.find((cat) => cat.id == "crime");
  const parent311 = activeCategories.find((cat) => cat.id == "311");

  return (
    <div>
      <hr />
      <div>
        <label for="preset">Presets</label>
        <select
          id="presetDate"
          name="presetDate"
            onChange={(e) => filterDateDispatcher({
                type: "calcDate",
                value: e.target.value
            })}
        >
          <option value="1"> 1 day</option>
          <option value="3"> 3 days</option>
          <option value="7"> 1 week</option>
          <option value="14"> 2 weeks</option>
          <option value="month"> 1 month </option>
        </select>
      </div>
      <div>
        <label for="start">Start:</label>
        <input
          type="date"
          id="startDate"
          name="start"
          value={filterDate.date.startDate}
          min={startMinDate}
          max={startMaxDate}
          onChange={(e) =>
            filterDateDispatcher({
              type: "setDate",
              date: {
                startDate: e.target.value,
              },
            })
          }
        />
      </div>

      <div>
        <label for="end">End:</label>
        <input
          type="date"
          id="endDate"
          name="end"
          value={filterDate.date.endDate}
          min={endMinDate}
          max={endMaxDate}
          onChange={(e) =>
            filterDateDispatcher({
              type: "setDate",
              date: {
                endDate: e.target.value,
              },
            })
          }
        />
      </div>
      <hr />

      <ul>
        <CheckBoxGroup
          key={`group-${parentCrime.id}`}
          parent={parentCrime}
          categories={activeCategories}
          activeCategoriesDispatcher={activeCategoriesDispatcher}
        />
      </ul>
      <ul>
        <CheckBoxGroup
          key={`group-${parent311.id}`}
          parent={parent311}
          categories={activeCategories}
          activeCategoriesDispatcher={activeCategoriesDispatcher}
        />
      </ul>
    </div>
  );
}
