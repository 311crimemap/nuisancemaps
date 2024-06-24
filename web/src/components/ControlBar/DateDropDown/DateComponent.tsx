import { useState, useMemo, useEffect, useCallback } from "react";
import debounce from "lodash/debounce";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import InputDate from "./InputDate";

export function DateComponent({ filterDate, filterDateDispatcher }) {
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

  const [icon, setIcon] = useState(faChevronUp);
  const [isBusy, setIsBusy] = useState(false);

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

  const onClickHandler = (e) => {
    console.log("[label] CLICK", isBusy);
    if (document.activeElement == e.currentTarget && !isBusy) {
      e.currentTarget.blur(); //close?
    }
  };

  //only request on valid date ranges
  useEffect(() => {
    if (inputDate.startDate.isValid && inputDate.endDate.isValid) {
      debounceFilterDateDispatcher({
        type: "setDate",
        date: {
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

  const setIconDown = () => {
    setIsBusy(true);
    setIcon(faChevronDown);

    //without delay of setIsCloseable toggle, the onClickHandler will
    //see div with focus, and close immediately - looks like a flash open/close.
    //By adding delay, we can make sure dropdown stays open until a subsequent click.
    setTimeout(() => {
      setIsBusy(false);
    }, 50);
  };
  return (
    <div className="dropdown dropdown-bottom flex">
      {/* Dropdown */}

      <div
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 sm:px-4 bg-base-100 hover:bg-secondary-content focus:border-indigo-300 focus:bg-neutral-content/75"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIconDown()}
        onClick={onClickHandler}
      >
        Date
        <FontAwesomeIcon icon={icon} />
      </div>

      {/* Date */}

      <ul
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-80"
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
              minDate={startMinDate}
              maxDate={startMaxDate}
              inputDate={inputDate}
              setInputDate={setInputDate}
            />
          </div>

          <div>
            <label for="end">End:</label>
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
      </ul>
    </div>
  );
}
