import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

export function DateComponent({ filterDate, filterDateDispatcher }) {
  const [icon, setIcon] = useState(faChevronUp);
  const min = new Date();
  min.setDate(min.getDate() - 365);
  const max = new Date();

  //en-CA? need YYYY-MM-DD format string for <input>
  const startMinDate = min.toLocaleDateString("en-CA");
  const startMaxDate = max.toLocaleDateString("en-CA");
  const endMinDate = min.toLocaleDateString("en-CA");
  const endMaxDate = max.toLocaleDateString("en-CA");

  return (
    <div className="dropdown dropdown-bottom flex w-full">
      {/* Dropdown */}

      <div
        tabIndex={0}
        role="button"
        className="btn btn-sm"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIcon(faChevronDown)}
      >
        Date Range
        <FontAwesomeIcon icon={icon} />
      </div>

      {/* Date */}

      <ul
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-52"
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
        </div>
      </ul>
    </div>
  );
}
