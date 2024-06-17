import { useState, useMemo, useCallback, useEffect } from "react";
import debounce from "lodash/debounce";
import validator from "validator";

export default function InputDate({
  id,
  name,
  minDate,
  maxDate,
  inputDate,
  setInputDate,
}) {
  const [isDateError, setIsDateError] = useState("");

  /*
   * inputDateValue: displayed date controlled two ways:
   *
   * 1. set manually here by control (controlDate)
   * 2. passed as prop (via date presets); through set inputDate object;
   *   reloaded via useEffect
   */
  const [inputDateValue, setInputDateValue] = useState(inputDate[id].date);

  const setDateHandler = (e, name) => {
    const controlDate = e.target.value;

    const isValid =
      validator.isDate(controlDate) &&
      validator.isAfter(controlDate, minDate) &&
      validator.isBefore(controlDate, maxDate);

    if (isValid) {
      setIsDateError("");
    } else {
      setIsDateError("input-error");
    }

    //update immediate control value
    setInputDateValue(controlDate);

    //update parent value
    setInputDate({
      ...inputDate,
      [id]: {
        isValid,
        date: controlDate,
      },
    });
  };

  //need to refresh on prop change
  //in the case where date presets are selected
  useEffect(() => {
    setInputDateValue(inputDate[id].date);
  }, [inputDate[id].date]);

  return (
    <input
      type="date"
      id={id}
      name={name}
      value={inputDateValue}
      min={minDate}
      max={maxDate}
      onChange={(e) => setDateHandler(e, name)}
      className={`input input-bordered ${isDateError}`}
    />
  );
}
