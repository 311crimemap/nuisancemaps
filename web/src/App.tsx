import {useState, useEffect} from 'react';
import './App.css'

import MapComponent from "./MapComponent";
import Sidebar from "./SidebarComponent.tsx";

function App() {
    const style = {
        display: 'flex'
    }

    const [map, setMap] = useState(null);

    useEffect(() => {
        console.log("MAP", map);
    }, [map])

    return (
        <>
            <div style={style}>

                <MapComponent map={map} setMap={setMap} />

                <Sidebar map={map} />

            </div>

            <div id="base">
                Base: crimes in view
            </div>
        </>
    )
}

export default App
