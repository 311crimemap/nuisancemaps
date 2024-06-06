import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";

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
  const parent = activeCategories.find((cat) => cat.id == type);

  return (
    <div className="dropdown dropdown-bottom flex">
      <div
        tabIndex={0}
        role="button"
        className="btn m-2"
        onBlur={() => setIcon(faChevronUp)}
        onFocus={() => setIcon(faChevronDown)}
      >
        {type}
        <FontAwesomeIcon icon={icon} />
      </div>

      <ul
        className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box"
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
