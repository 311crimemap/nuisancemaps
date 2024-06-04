import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'

export function Search() {
    return (
        <label className="input input-bordered">
            <input type="text" className="grow" placeholder="Search" />
            <FontAwesomeIcon icon={faSearch} />
        </label>
    )

}
