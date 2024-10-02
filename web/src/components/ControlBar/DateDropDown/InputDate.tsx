import { useState, Dispatch, useEffect, SetStateAction } from "react";
import { InputDateRange } from "../../../types/daterange.ts";
import validator from "validator";

interface InputDateProps {
  id: "startDate" | "endDate";
  name: string;
  minDate: string;
  maxDate: string;
  inputDate: InputDateRange;
  setInputDate: Dispatch<SetStateAction<InputDateRange>>;
}

export default function InputDate({
  id,
  name,
  minDate,
  maxDate,
  inputDate,
  setInputDate,
}: InputDateProps) {
  const [isDateError, setIsDateError] = useState("");

  /*
   * inputDateValue: displayed date controlled two ways:
   *
   * 1. set manually here by control (controlDate)
   * 2. passed as prop (via date presets); through set inputDate object;
   *   reloaded via useEffect
   */

  const defaultDateValue = inputDate[id].date;
  const [inputDateValue, setInputDateValue] = useState(defaultDateValue);

  const setDateHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
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
      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setDateHandler(e)}
      className={`w-full input input-bordered ${isDateError}`}
    />
  );
}
