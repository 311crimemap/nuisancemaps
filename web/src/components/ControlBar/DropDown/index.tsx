import react from "react";
import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

export default function DropDown({ children }) {
  const childArray = react.Children.toArray(children);

  const [icon, setIcon] = useState(faChevronUp);
  const [isBusy, setIsBusy] = useState(false);

  const onClickHandler = (e) => {
    console.log("[label] CLICK", isBusy);
    if (document.activeElement == e.currentTarget && !isBusy) {
      e.currentTarget.blur(); //close
    }
  };

  const setIconDown = () => {
    setIsBusy(true);
    setIcon(faChevronDown);

    //without added delay with isBusy toggle, the onClickHandler will
    //see div with focus and close immediately, resulting in a flash open/close.
    //
    //By adding delay, we can make sure dropdown stays open until a subsequent click.
    setTimeout(() => {
      setIsBusy(false);
    }, 100);
  };

  return (
    <div className="dropdown dropdown-bottom flex">
      <div
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 sm:px-4 capitalize bg-base-100 hover:bg-secondary-content focus:border-indigo-300"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIconDown()}
        onClick={onClickHandler}
      >
        {childArray[0]}
        <FontAwesomeIcon icon={icon} />
      </div>

      <ul
        tabIndex={0}
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-full w-72"
        onFocus={() => setIcon(faChevronDown)}
      >
        {childArray[1]}
      </ul>
    </div>
  );
}
