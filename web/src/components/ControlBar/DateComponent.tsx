import { useState } from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'

function DropDown() {
    const [icon, setIcon] = useState(faChevronUp);

    return (
        <div className="dropdown dropdown-bottom">
            <div tabIndex={0} role="button" className="btn btn-sm"
                onBlur={() => setIcon(faChevronUp)}
                onFocus={() => setIcon(faChevronDown)}
            >
                Date Range
                <FontAwesomeIcon icon={icon} />
            </div>

            {
                /*
                not sure radio, select - exclusive selection
                or something that populates?
              */
            }

            <ul className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-52"
                onBlur={() => setIcon(faChevronUp)}
                onFocus={() => setIcon(faChevronDown)}>
                <ul>
                    <li>1 day</li>
                    <li>3 day</li>
                </ul>

                <div>
                    <input type="date" />
                    <input type="date" />
                </div>
            </ul>

        </div>
    )
}

export function DateComponent() {
  return (
      <div>
          <DropDown />
      </div>
  );
}
