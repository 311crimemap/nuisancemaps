import { useState, Dispatch, SetStateAction } from "react";
import { Map, LayerSpecification } from "maplibre-gl";
import { DATASOURCES } from "../../../types/datasources";

interface ToggleComponentProps {
  map: Map;
  setActiveFeatures: Dispatch<SetStateAction<{}>>;
}

export function ToggleComponent({
  map,
  setActiveFeatures,
}: ToggleComponentProps) {
  const [isActive, setIsActive] = useState(false);

  const toggleHandler = () => {
    const layers: LayerSpecification[] = map.getStyle().layers;

    const sources = [DATASOURCES.DataCrimes, DATASOURCES.Data311s];
    const heatMapSources = [
      DATASOURCES.HeatMapDataCrimes,
      DATASOURCES.HeatMapData311s,
    ];

    //heatmap toggle on
    for (const heatMapSource of heatMapSources) {
      const sourceLayers = layers.filter((layer) => {
        const layerWithSource = layer as { source: string };
        return layerWithSource.source === heatMapSource;
      });
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
      const sourceLayers = layers.filter((layer) => {
        const layerWithSource = layer as { source: string };
        return layerWithSource.source === source;
      });

      for (const sl of sourceLayers) {
        map.setLayoutProperty(
          sl.id,
          "visibility",
          isActive ? "visible" : "none"
        );
      }
    }

    setActiveFeatures({}); //clear any active display
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
