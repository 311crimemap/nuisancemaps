import { useState, useEffect, useReducer } from "react";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";

export default function FiltersView({
  map,
  activeCategories,
  activeCategoriesDispatcher,
}) {
  const min = new Date();
  min.setDate(min.getDate() - 365);
  const max = new Date();

  //en-CA? need YYYY-MM-DD format string for <input>
  const startMinDate = min.toLocaleDateString("en-CA");
  const startMaxDate = max.toLocaleDateString("en-CA");
  const endMinDate = min.toLocaleDateString("en-CA");
  const endMaxDate = max.toLocaleDateString("en-CA");

  const [startDate, setStartDate] = useState(startMaxDate);
  const [endDate, setEndDate] = useState(endMaxDate);

  const parentCrime = activeCategories.find((cat) => cat.id == "crime");
  const parent311 = activeCategories.find((cat) => cat.id == "311");


  const presetDateHandler = (value) => {
    console.log("presetDateHandler", value);

    const calcDate = new Date();
    setEndDate(endMaxDate);

    if (value == "month") {
      calcDate.setMonth(calcDate.getMonth() - 1);
      setStartDate(calcDate.toLocaleDateString("en-CA"));
      return;
    }

    if (Number(value) != NaN) {
      calcDate.setDate(calcDate.getDate() - Number(value));
      setStartDate(calcDate.toLocaleDateString("en-CA"));
      return;
    }
  };

  return (
    <div>
      <hr />
      <div>
        <label for="preset">Presets</label>
        <select
          id="presetDate"
          name="presetDate"
          onChange={(e) => presetDateHandler(e.target.value)}
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
          value={startDate}
          min={startMinDate}
          max={startMaxDate}
          onChange={(e) => setStartDate(e.target.value)}
        />
      </div>

      <div>
        <label for="end">End:</label>
        <input
          type="date"
          id="endDate"
          name="end"
          value={endDate}
          min={endMinDate}
          max={endMaxDate}
          onChange={(e) => setEndDate(e.target.value)}
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
