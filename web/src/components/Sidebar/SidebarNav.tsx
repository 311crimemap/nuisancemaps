import {View} from "./View";

export default function SidebarNav({view, setView}) {

    const viewToggleClickHandler = (e, view: View) => {
        setView(view);
    };

    //TODO: active/inactvie style with view
    return (
        <ul id="sidebar-nav">

            <li onClick={(e) => viewToggleClickHandler(e, View.INCIDENTS)}>
                Incidents
            </li>


            <li onClick={(e) => viewToggleClickHandler(e, View.FILTERS)}>
                Filters
            </li>

        </ul >
    )
}
