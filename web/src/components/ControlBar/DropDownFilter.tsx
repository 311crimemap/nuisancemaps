import { useState } from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'

/*
 * use dropdown button versus <details> / <summary> tags
 * because dropdown will close on click elsewhere;
 * detail / summary remains open unless explicitly clicked to minimize parent
 */
export function DropDownFilter(props: any) {
    const [icon, setIcon] = useState(faChevronUp);

    return (

        <div className="dropdown dropdown-bottom">

            <div tabIndex={0} role="button" className="btn btn-sm"
                onBlur={() => setIcon(faChevronUp)}
                onFocus={() => setIcon(faChevronDown)}>
                {props.type}
                <FontAwesomeIcon icon={icon} />
            </div>

            <ul className="dropdown-content z-[1] menu shadow p-2 bg-base-100 rounded-box w-52"
                onFocus={() => setIcon(faChevronDown)}
            >
                Stuff

            </ul>

        </div>
    )
}
