import react from "react";
import { useState, useRef, useEffect, useMemo, useCallback } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

export default function DropDown({ children }) {
  const buttonRef = useRef(null);

  const childArray = react.Children.toArray(children);
  const [icon, setIcon] = useState(faChevronUp);
  const [isOpen, setIsOpen] = useState(false);

  const clearRefTimeout = () => {
    if (buttonRef.current) {
      clearTimeout(buttonRef.current);
    }
  };

  //"closed"
  const onBlurHandler = (e) => {
    setIcon(faChevronUp);
    setIsOpen(false);
    clearRefTimeout();
  };

  //"open"
  const onFocusHandler = (e) => {
    setIcon(faChevronDown);

    //delay setIsOpen with timeout so onClick actually closes.
    //when dropdown closed is clicked,
    //
    //onFocus fires setting isOpen to true, and then onClick fires, so the
    //dropdown immediately flash closes.
    //
    //adding a delay makes the onClick be able to open and close.
    //
    //TODO: small bug where click twice to close after
    //toggling checkboxes.

    buttonRef.current = setTimeout(() => {
      setIsOpen(true);
    }, 125);
  };

  // want to add 'close on click' behavior not found in daisyUI dropdowns.
  const onClickHandler = (e) => {
    if (isOpen) {
      e.currentTarget.blur();
      setIsOpen(false);
    }
  };

  useEffect(() => {
    return () => clearRefTimeout();
  }, []);

  console.log("Render", isOpen);
  return (
    <div className="flex dropdown dropdown-bottom dropdown-responsive">
      <div
        ref={buttonRef}
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 sm:px-4 capitalize bg-base-100 hover:bg-secondary-content focus:border-indigo-300"
        onClick={(e) => onClickHandler(e)}
        onFocus={(e) => onFocusHandler(e)}
        onBlur={(e) => onBlurHandler(e)}
      >
        {childArray[0]}
        <FontAwesomeIcon icon={icon} />
      </div>

      <ul
        tabIndex={0}
        className="dropdown-content z-[1] menu menu-xs shadow p-2 bg-base-100 rounded w-80 max-h-[70vh] overflow-y-auto overflow-x-hidden"
        onFocus={(e) => onFocusHandler(e)}
        onBlur={(e) => onBlurHandler(e)}
      >
        {childArray[1]}
      </ul>
    </div>
  );
}
