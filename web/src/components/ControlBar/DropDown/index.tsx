import react from "react";
import { useState, useRef, useEffect, useMemo, useCallback } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

export default function DropDown({ children }) {
  const childArray = react.Children.toArray(children);
  const [icon, setIcon] = useState(faChevronUp);

  const onBlurHandler = (e) => {
    setIcon(faChevronUp);
  };

  const onFocusHandler = (e) => {
    setIcon(faChevronDown);
  };

  return (
    <div className="flex dropdown dropdown-bottom dropdown-responsive">
      <div
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 sm:px-4 capitalize bg-base-100 hover:bg-secondary-content focus:border-indigo-300"
        onBlur={(e) => onBlurHandler(e)}
        onFocus={(e) => onFocusHandler(e)}
      >
        {childArray[0]}
        <FontAwesomeIcon icon={icon} />
      </div>

      <ul
        tabIndex={0}
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded w-80 max-h-[70vh] overflow-y-auto overflow-x-hidden"
        onFocus={() => setIcon(faChevronDown)}
      >
        {childArray[1]}
      </ul>
    </div>
  );
}
