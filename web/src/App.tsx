import { useState, useEffect } from "react";
import "./App.css";

import MapComponent from "./MapComponent";
import Sidebar from "./SidebarComponent.tsx";
import BaseComponent from "./BaseComponent.tsx";

function App() {
  const style = {
    display: "flex",
  };

  const [map, setMap] = useState(null);
  const [position, setPosition] = useState({
    center: [-97.7171, 30.2944],
  });

  const defaultData = {
    type: "FeatureCollection",
    features: [],
  };
  const [dataCrimes, setDataCrimes] = useState(defaultData);
  const [data311s, setData311s] = useState(defaultData);

  const getData = async (url: string) => {
    return fetch(url).then((res) => res.json());
  };

  useEffect(() => {
    console.log("FETCH");
    const limit = 500;
    const center = position.center;
    const dataCrimesURL = `http://localhost:8080/datacrimes.geojson?center=${center}&limit=${limit}`;
    const data311sURL = `http://localhost:8080/data311s.geojson?center=${center}limit=${limit}`;

    Promise.all([getData(dataCrimesURL), getData(data311sURL)]).then(
      ([dataCrimes, data311s]) => {
        setDataCrimes(dataCrimes);
        setData311s(data311s);
      }
    );

    //to make new request
    //position.center - too sensitive, even zoom will trigger
  }, []);

  console.log("RENDER", position, dataCrimes);
  return (
    <>
      <div style={style}>
        <MapComponent
          map={map}
          setMap={setMap}
          position={position}
          setPosition={setPosition}
          dataCrimes={dataCrimes}
          data311s={data311s}
        />

        <Sidebar map={map} />
      </div>

      <BaseComponent map={map} />
    </>
  );
}

export default App;
