import { useState } from "react";

import validator from "validator";

export default function InputDate({
  id,
  name,
  date,
  minDate,
  maxDate,
  filterDateDispatcher,
}) {
  const [isDateError, setIsDateError] = useState("");

  const setDateHandler = (e, dateType) => {
    const inputDate = e.target.value;

    const date = {};
    date[dateType] = inputDate;

    const isValid =
      validator.isDate(inputDate) &&
      validator.isAfter(inputDate, minDate) &&
      validator.isBefore(inputDate, maxDate);

    if (isValid) {
      setIsDateError("");
    } else {
      setIsDateError("input-error");
    }

    //  bigger goal is to separate the validation from firing requests (evnetualy debounce requests)
    filterDateDispatcher({
      type: "setDate",
      date,
    });
  };

  return (
    <input
      type="date"
      id={id}
      name={name}
      value={date}
      min={minDate}
      max={maxDate}
      onChange={(e) => setDateHandler(e, name)}
      className={`input input-bordered ${isDateError}`}
    />
  );
}
