import { useState } from "react";

export function ToggleComponent({ map, DATASOURCES, setActiveFeatureList }) {
  const [isActive, setIsActive] = useState(false);

  const toggleHandler = (e) => {
    const layers = map.getStyle().layers;

    const sources = [DATASOURCES.DataCrimes, DATASOURCES.Data311s];
    const heatMapSources = [
      DATASOURCES.HeatMapDataCrimes,
      DATASOURCES.HeatMapData311s,
    ];

    //heatmap toggle on
    for (const heatMapSource of heatMapSources) {
      const sourceLayers = layers.filter(
        (layer) => layer.source == heatMapSource
      );
      for (const sl of sourceLayers) {
        map.setLayoutProperty(
          sl.id,
          "visibility",
          !isActive ? "visible" : "none" //remember we're toggling
        );
      }
    }

    //counter cluster and point sources toggle off
    for (const source of sources) {
      const sourceLayers = layers.filter((layer) => layer.source == source);
      for (const sl of sourceLayers) {
        map.setLayoutProperty(
          sl.id,
          "visibility",
          isActive ? "visible" : "none"
        );
      }
    }

    setActiveFeatureList({}); //clear any active display
    setIsActive(!isActive);
  };

  return (
    <>
      <input
        type="checkbox"
        className="toggle toggle-xs sm:toggle-sm ml-2 mb-1"
        checked={isActive}
        onChange={toggleHandler}
      />
      <span className="ml-2 text-xs">Heatmap</span>
    </>
  );
}
