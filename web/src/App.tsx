import {useState, useEffect} from 'react';
import './App.css'

import MapComponent from "./MapComponent";
import Sidebar from "./SidebarComponent.tsx";
import BaseComponent from "./BaseComponent.tsx";

function App() {
    const style = {
        display: 'flex'
    }

    const [map, setMap] = useState(null);

    useEffect(() => {
        console.log("MAP", map);
        //console.log(map && map.getStyle().layers);
    }, [map])

    return (
        <>
            <div style={style}>

                <MapComponent map={map} setMap={setMap} />

                <Sidebar map={map} />

            </div>

            <BaseComponent map={map} />
        </>
    )
}

export default App
