import React from "react";
import { useState, useRef, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

interface DropDownProps {
    children: React.ReactNode
}
export default function DropDown({ children }: DropDownProps) {
  const buttonRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const childArray = React.Children.toArray(children);
  const [icon, setIcon] = useState(faChevronUp);
  const [isOpen, setIsOpen] = useState(false);

  const clearRefTimeout = () => {
    if (buttonRef.current) {
      clearTimeout(buttonRef.current);
    }
  };

  //"closed"
  const onBlurHandler = () => {
    setIcon(faChevronUp);
    setIsOpen(false);
    clearRefTimeout();
  };

  /* To enable a natural "click-to-close" behavior on these daisyUI dropdowns,
     we need to modify onFocus and onClick event handlers to prevent flash
     open/close onClick behavior.

   * onFocusHandler: used to "open" dropdown - this is how daisyUI naturally
     opens the dropdowns. Add a setTimeout delay so that to open, the click
     event bypasses the focus event without triggering a close. The delay
     lets SetIsOpen fire after the click event has completed.

   * onClickHandler: used only to "close" dropdown when state is open.

     Essentially a click fires first a focus, and then immediately a click
     event.

     We use focus to open, since that's how daisyUI natively opens a
     dropdown.

     Add a delay for setIsOpen so onClick fires as a no op, as there is
     nothign to close.

     When later click-to-close, dropdown is already in focus so "open" event
     never fires, so onClick simply blurs close.
   */

  //"open"
  const onFocusHandler = () => {
    setIcon(faChevronDown);

    //TODO: small bug where click twice to close after
    //toggling checkboxes.
    buttonRef.current = setTimeout(() => {
      setIsOpen(true);
    }, 125);
  };

  // handling in partnershipw ith onFocusHandler to add 'close on click'
  // behavior not found in daisyUI dropdowns.
  const onClickHandler = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    if (isOpen) {
      e.currentTarget.blur();
      setIsOpen(false);
    }
  };

  useEffect(() => {
    return () => clearRefTimeout();
  }, []);

  return (
    <div className="flex dropdown dropdown-bottom dropdown-responsive">
      <div
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 xl:px-4 capitalize bg-base-100 hover:bg-secondary-content focus:border-indigo-300"
        onClick={(e: React.MouseEvent<HTMLDivElement, MouseEvent>) =>
          onClickHandler(e)
        }
        onFocus={() => onFocusHandler()}
        onBlur={() => onBlurHandler()}
      >
        {childArray[0]}
        <FontAwesomeIcon className="dropdown-chevron" icon={icon} />
      </div>

      <ul
        tabIndex={0}
        className="dropdown-content z-[1] menu menu-xs shadow p-4 bg-base-100 rounded w-80 max-h-[70vh] overflow-y-auto overflow-x-hidden"
        onFocus={() => onFocusHandler()}
        onBlur={() => onBlurHandler()}
      >
        {childArray[1]}
      </ul>
    </div>
  );
}
