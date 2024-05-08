import { useState, useEffect } from "react";
import "./App.css";

import useMap from "./components/Map/useMap";
import { MapComponent } from "./components/Map";
import { Sidebar } from "./components/Sidebar";
import { SpiderListComponent } from "./components/SpiderList";
import BottomSheetComponent from "./BottomSheetComponent.tsx";

function App() {

  const spiderZoomLevel = 17

  const [position, setPosition] = useState({
    center: [-97.7171, 30.2944],
  });

  const defaultData = {
    type: "FeatureCollection",
    features: [],
  };

  const [dataCrimes, setDataCrimes] = useState(defaultData);
  const [data311s, setData311s] = useState(defaultData);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [activeReportNum, setActiveReportNum] = useState(null);
  const [activeSpiderList, setActiveSpiderList] = useState([]);

  const getData = async (url: string) => {
    return fetch(url).then((res) => res.json());
  };

  useEffect(() => {
    console.log("FETCH");
    const limit = 500;
    const center = position.center;
    const dataCrimesURL = `http://localhost:8080/datacrimes?center=${center}&limit=${limit}`;
    const data311sURL = `http://localhost:8080/data311s?center=${center}&limit=${limit}`;

    Promise.all([getData(dataCrimesURL), getData(data311sURL)]).then(
      ([dataCrimes, data311s]) => {
        setDataCrimes(dataCrimes);
        setData311s(data311s);
        setIsDataLoaded(true);
      }
    );

    //to make new request
    //position.center - too sensitive, even zoom will trigger
  }, []);


  const map = useMap({
      position, setPosition,
      activeReportNum, setActiveReportNum,
      dataCrimes, data311s,
      setActiveSpiderList,
      spiderZoomLevel,
      isDataLoaded
  })

  console.log("[App] Render", position, activeReportNum);
  return (
        <>
            <div id="container">

                <Sidebar map={map} activeReportNum={activeReportNum} setActiveReportNum={setActiveReportNum} />

                <MapComponent
                    map={map}
                    position={position}
                    setPosition={setPosition}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                    setActiveSpiderList={setActiveSpiderList}
                    dataCrimes={dataCrimes}
                    data311s={data311s}
                />

                <SpiderListComponent map={map}
                                     spiderZoomLevel={spiderZoomLevel}
                                     activeSpiderList={activeSpiderList}
                                     activeReportNum={activeReportNum} setActiveReportNum={setActiveReportNum} />
            </div>

            <BottomSheetComponent map={map} />
        </>
    );
}

export default App;
