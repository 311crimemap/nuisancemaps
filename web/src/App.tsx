import { useState, useEffect, useReducer, useRef } from "react";
import { useParams } from "react-router-dom";
import "./App.css";
import Meta from "./Meta";
import Categories from "./components/Map/categories";
import useMap from "./components/Map/useMap";
import { ControlBar } from "./components/ControlBar";
import { MapComponent } from "./components/Map";
import { FeatureListComponent } from "./components/FeatureList";
import categoryCheckBoxReducer from "./components/ControlBar/CategoryDropDown/CategoryFilterReducer";
import dateFilterReducer from "./components/ControlBar/DateDropDown/DateFilterReducer";
import { DATASOURCES, DataSourcesMap } from "./types/datasources.ts";
import { DATASTATUS } from "./types/datastatus.ts";
import { calcMaxLatLngBounds, deslugify } from "./Util";
import { getData } from "./Util";
import { defaultData } from "./types/datafeatures.ts";
import { defaultDateRange } from "./types/daterange";
import { MapPosition, defaultMapPosition } from "./types/mapposition";
import { defaultActiveFeatures } from "./types/activefeatures.ts";
import { Log } from "./Logger";
import { MapStatus } from "./components/MapStatus";

function App() {
  const { DEV, MODE, PROD } = { ...import.meta.env };
  Log.log({
    msg: "ENV",
    params: { env: { DEV, MODE, PROD } },
  });
  const { city } = useParams();
  const featureZoomLevel = 17;

  const [position, setPosition] = useState<MapPosition>(defaultMapPosition);
  const [dataCrimes, setDataCrimes] = useState(defaultData);
  const [data311s, setData311s] = useState(defaultData);
  const [showOlder, setShowOlder] = useState(true);

  const [initStatus, setInitStatus] = useState(DATASTATUS.LOADING);
  const [dataStatus, setDataStatus] = useState(DATASTATUS.NONE);
  const lastRequestKey = useRef<string | null>(null);

  const [activeReportNum, setActiveReportNum] = useState(null);
  const [activeFeatures, setActiveFeatures] = useState(defaultActiveFeatures);
  const [sources, setSources] = useState(defaultData);
  const [categories, setCategories] = useState([]);
  const [activeCategories, activeCategoriesDispatcher] = useReducer(
    categoryCheckBoxReducer,
    []
  );
  const [filterDate, filterDateDispatcher] = useReducer(
    dateFilterReducer,
    defaultDateRange
  );

  useEffect(() => {
    setActiveFeatures({ source: "", features: [] });
  }, [showOlder]);

  const dataSources: DataSourcesMap = {
    [DATASOURCES.Sources]: {
      type: "geojson",
      data: sources, // zoomed out city points
      cluster: true,
      clusterMaxZoom: 14,
      clusterRadius: 20,
    },
    [DATASOURCES.Data311s]: {
      type: "geojson",
      data: data311s,
      cluster: true,
      clusterMaxZoom: 18, // Max zoom to cluster points on
      clusterRadius: 50, // Radius of each cluster when clustering points (defaults to 50)
      clusterProperties: { older_count: ["+", ["get", "olderCount"]] },
    },
    [DATASOURCES.DataCrimes]: {
      type: "geojson",
      data: dataCrimes,
      cluster: true,
      clusterMaxZoom: 18, // Max zoom to cluster points on
      clusterRadius: 50, // Radius of each cluster when clustering points (defaults to 50)
      clusterProperties: { older_count: ["+", ["get", "olderCount"]] },
    },

    //TODO: issue w/ dueling layers - they get overlaid not aggregated (like clusters)
    //so not desirable for heatmaps...
    [DATASOURCES.HeatMapDataCrimes]: {
      type: "geojson",
      data: dataCrimes,
      cluster: false,
    },
    [DATASOURCES.HeatMapData311s]: {
      type: "geojson",
      data: data311s,
      cluster: false,
    },
  };

  /*
   * INIT FETCH
   */

  useEffect(() => {
    const initURL = `${import.meta.env.VITE_API_SERVER_URL}/init`;
    setInitStatus(DATASTATUS.LOADING);

    Promise.all([
      categories.length == 0 ? getData(initURL) : Promise.resolve(categories),
    ])
      .then(([dataSourceCategories]) => {
        //preserve any checked filters
        if (categories.length == 0) {
          setCategories(dataSourceCategories.data.categories);
          setSources(dataSourceCategories.data.sources);

          activeCategoriesDispatcher({
            type: "init",
            categories: Categories.buildHierarchy(
              dataSourceCategories.data.categories
            ),
          });
        }

        filterDateDispatcher({
          type: "isBusy",
          value: false,
        });
        //throw new Error();
        setInitStatus(DATASTATUS.OK);
      })
      .catch(() => {
        setInitStatus(DATASTATUS.ERROR);
      });
  }, [initStatus > DATASTATUS.NONE]);

  // want to initalize map first
  // to populate position obj
  const { map, mapController } = useMap({
    position,
    setPosition,
    dataCrimes,
    data311s,
    setActiveReportNum,
    dataSources,
    setActiveFeatures,
    featureZoomLevel,
    isInitLoaded: initStatus === DATASTATUS.OK,
  });

  /*
   * DATA FETCH
   */
  useEffect(() => {
    if (initStatus !== DATASTATUS.OK) return;
    if (dataStatus === DATASTATUS.LOADING) return;
    if (!map) return;
    if (map.getZoom() < 10) return;

    const bounds = map.getBounds();
    const fetchBounds =
      position.fetchBounds || calcMaxLatLngBounds(bounds, map.getZoom());

    // setting position fetchBounds here avoids a duplicate fetch
    // (useMap onMove sets refresh true if no fetchBounds)

    const params = new URLSearchParams({
      startDate: filterDate.date.startDate,
      endDate: filterDate.date.endDate,
      //mute lat/lng to make uri more cacheable
      //lat,
      //lng,
      sw_lat: fetchBounds.getSouthWest().lat.toString(),
      sw_lng: fetchBounds.getSouthWest().lng.toString(),
      ne_lat: fetchBounds.getNorthEast().lat.toString(),
      ne_lng: fetchBounds.getNorthEast().lng.toString(),
    });
    const requestKey = params.toString();
    if (lastRequestKey.current === requestKey) return;
    lastRequestKey.current = requestKey;

    filterDateDispatcher({
      type: "isBusy",
      value: true,
    });

    const dataCrimesURL = `${
      import.meta.env.VITE_API_SERVER_URL
    }/datacrimes.geojson?${params.toString()}`;
    const data311sURL = `${
      import.meta.env.VITE_API_SERVER_URL
    }/data311s.geojson?${params.toString()}`;

    setDataStatus(DATASTATUS.LOADING);

    Log.log({
      msg: "Fetch Data",
      params: { dataCrimesURL, data311sURL },
      useEffect: {
        isInitLoaded: initStatus === DATASTATUS.OK,
        refresh: position.refresh,
        date: filterDate.date,
        map,
      },
      ...Log.data,
    });

    Promise.all([getData(dataCrimesURL), getData(data311sURL)])
      .then(([dataCrimes, data311s]) => {
        setDataCrimes(dataCrimes);
        setData311s(data311s);
        setDataStatus(DATASTATUS.OK);
      })
      .catch(() => {
        setDataStatus(DATASTATUS.ERROR);
      })
      .finally(() => {
        filterDateDispatcher({
          type: "isBusy",
          value: false,
        });
      });

    //
    // trigger new fetch by watching position.refresh counter
    // (NB: setting change on viewport's position.center is too sensitive/disruptive,
    // even a zoom will trigger unecessary fetches because underlying data doesn't change.)
    //
    // position.refresh initial value: 0; increments to 1 on useMap onMove
    //
    // there is a double-fetch issue when visiting /cities page and clicking to city
    // this is resolved by checking if(DATASTATUS.LOADING) guard in above fetch useEffect
    // but note without it it's a consistent issue
    //
  }, [
    map,
    //isInitLoaded,
    initStatus !== DATASTATUS.LOADING,
    position.refresh,
    filterDate.date.startDate,
    filterDate.date.endDate,
    dataStatus,
  ]);

  Log.log({
    msg: "Render",
    params: { position, activeReportNum, map, mapController },
    ...Log.data,
  });

  /* RENDER */

  //init MapStatus state
  if (initStatus !== DATASTATUS.OK) {
    return (
      <MapStatus
        dataStatus={initStatus}
        errorRefreshFn={() => setInitStatus(DATASTATUS.NONE)}
      />
    );
  }

  return (
    <>
      <Meta pageName={deslugify(city)} path={city} />
      <div className="mt-16">
        {map && mapController && (
          <ControlBar
            map={map}
            mapController={mapController}
            setActiveFeatures={setActiveFeatures}
            activeCategories={activeCategories}
            activeCategoriesDispatcher={activeCategoriesDispatcher}
            filterDate={filterDate}
            filterDateDispatcher={filterDateDispatcher}
            dataCrimes={dataCrimes}
            data311s={data311s}
            dataStatus={dataStatus}
            showOlder={showOlder}
            setShowOlder={setShowOlder}
          />
        )}
      </div>
      <div
        id="container"
        className={`${
          dataStatus === DATASTATUS.LOADING ? "opacity-50" : "opacity-100"
        }`}
      >
        <MapComponent
          map={map}
          position={position}
          setPosition={setPosition}
          activeReportNum={activeReportNum}
          setActiveReportNum={setActiveReportNum}
          activeCategories={activeCategories}
          DATASOURCES={DATASOURCES}
          dataCrimes={dataCrimes}
          data311s={data311s}
          showOlder={showOlder}
        />

        <FeatureListComponent activeFeatures={activeFeatures} />
      </div>

      <MapStatus
        dataStatus={dataStatus}
        errorRefreshFn={() => {
          lastRequestKey.current = null;
          setPosition({ ...position, refresh: position.refresh + 1 });
        }}
      />
    </>
  );
}

export default App;
