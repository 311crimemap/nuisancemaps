import { useState } from "react";

import debounce from "lodash/debounce";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import InputDate from "./InputDate";

export function DateComponent({ filterDate, filterDateDispatcher }) {
  const [icon, setIcon] = useState(faChevronUp);
  const min = new Date();
  min.setDate(min.getDate() - 365);
  const max = new Date();
  max.setDate(max.getDate() - 1);

  //en-CA? need YYYY-MM-DD format string for <input>
  const startMinDate = min.toLocaleDateString("en-CA");
  const startMaxDate = max.toLocaleDateString("en-CA");
  const endMinDate = min.toLocaleDateString("en-CA");
  const endMaxDate = max.toLocaleDateString("en-CA");

  return (
    <div className="dropdown dropdown-bottom flex">
      {/* Dropdown */}

      <div
        tabIndex={0}
        role="button"
        className="btn m-2"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIcon(faChevronDown)}
      >
        Date
        <FontAwesomeIcon icon={icon} />
      </div>

      {/* Date */}

      <ul
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-72"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIcon(faChevronDown)}
      >
        <div>
          <label for="preset">Presets</label>
          <select
            id="presetDate"
            name="presetDate"
            onChange={(e) =>
              filterDateDispatcher({
                type: "calcDate",
                value: e.target.value,
              })
            }
          >
            <option value="1"> 1 day</option>
            <option value="3"> 3 days</option>
            <option value="7"> 1 week</option>
            <option value="14"> 2 weeks</option>
            <option value="month"> 1 month </option>
          </select>
        </div>

        <div>
          <div>
            <label for="start">Start:</label>
            <InputDate
              id="startDate"
              name="startDate"
              date={filterDate.date.startDate}
              minDate={startMinDate}
              maxDate={startMaxDate}
              filterDateDispatcher={filterDateDispatcher}
            />
          </div>

          <div>
            <label for="end">End:</label>
            <InputDate
              id="endDate"
              name="endDate"
              date={filterDate.date.endDate}
              minDate={endMinDate}
              maxDate={endMaxDate}
              filterDateDispatcher={filterDateDispatcher}
            />
          </div>
        </div>
      </ul>
    </div>
  );
}
