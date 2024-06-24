import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";
import CheckBoxLabel from "./CheckBoxFilters/CheckBoxLabel";

/*
 * use dropdown button versus <details> / <summary> tags
 * because dropdown will close on click elsewhere;
 * detail / summary remains open unless explicitly clicked to minimize parent
 */
export function DropDownFilter({
  type,
  activeCategories,
  activeCategoriesDispatcher,
}) {
  const [icon, setIcon] = useState(faChevronUp);
  const [isBusy, setIsBusy] = useState(false);

  const parent = activeCategories.find((cat) => cat.id == type);

  const onClickHandler = (e) => {
    console.log("[label] CLICK", isBusy);
    if (document.activeElement == e.currentTarget && !isBusy) {
      e.currentTarget.blur(); //close?
    }
  };

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
      <div
        tabIndex={0}
        role="button"
        className="btn m-2 p-2 sm:px-4 capitalize bg-base-100 hover:bg-secondary-content focus:border-indigo-300"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIconDown()}
        onClick={onClickHandler}
      >
        <CheckBoxLabel
          key={`checkboxlabel-${parent.id}`}
          category={parent}
          categories={[]}
          activeCategoriesDispatcher={activeCategoriesDispatcher}
          inclusiveCheck={false}
        />
        <FontAwesomeIcon icon={icon} />
      </div>

      <ul
        tabIndex={0}
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-full"
        onFocus={() => setIcon(faChevronDown)}
      >
        <CheckBoxGroup
          key={`group-${parent.id}`}
          parent={parent}
          categories={activeCategories}
          activeCategoriesDispatcher={activeCategoriesDispatcher}
        />
      </ul>
    </div>
  );
}
