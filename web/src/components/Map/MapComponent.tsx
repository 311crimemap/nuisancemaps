import { useEffect } from "react";
import "maplibre-gl/dist/maplibre-gl.css";
import { DataFeature } from "../../types/datafeatures.ts";
import { Category } from "../../types/category.ts";

export default function MapComponent(props: any) {
  //given filter checkbox, filter at data level
  //
  //NB: need to do this at data level because clusters do not have any
  //underlying features - they are calculated.
  //so only way to adjust cluster counts is at data level
  useEffect(() => {
    if (!props.map) return;

    try {
      const dataCrimesSource = props.map.getSource(
        props.DATASOURCES.DataCrimes
      );
      const data311sSource = props.map.getSource(props.DATASOURCES.Data311s);
      const heatMapDataCrimesSource = props.map.getSource(
        props.DATASOURCES.HeatMapDataCrimes
      );
      const heatMapData311sSource = props.map.getSource(
        props.DATASOURCES.HeatMapData311s
      );

      console.log("PROPS", props.activeCategories, props.dataCrimes);
      const activeCategoriesIds = props.activeCategories
        .filter((c: Category) => c.checked)
        .map((c: Category) => c.id);

      const dataCrimes = {
        type: "FeatureCollection",
        features: props.dataCrimes.features.filter((feature: DataFeature) => {
          if (typeof feature.properties.category !== "string")
            return activeCategoriesIds.includes(
              feature.properties.category?.id
            );
          return false;
        }),
      };

      const data311s = {
        type: "FeatureCollection",
        features: props.data311s.features.filter((feature: DataFeature) => {
          if (typeof feature.properties.category !== "string")
            return activeCategoriesIds.includes(
              feature.properties.category?.id
            );
          return false;
        }),
      };

      dataCrimesSource?.setData(dataCrimes);
      data311sSource?.setData(data311s);
      heatMapDataCrimesSource?.setData(dataCrimes);
      heatMapData311sSource?.setData(data311s);
    } catch (e) {
      console.error(e);
    }
  }, [props.activeCategories, props.dataCrimes, props.data311s]);

  return <div id="map"></div>;
}
