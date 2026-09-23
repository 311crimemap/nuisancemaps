import { useEffect } from "react";
import "maplibre-gl/dist/maplibre-gl.css";
import { DataFeature } from "../../types/datafeatures.ts";
import { Category } from "../../types/category.ts";
import { Log } from "../../Logger";

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

      const activeCategoriesIds = props.activeCategories
        .filter((c: Category) => c.checked)
        .map((c: Category) => c.id);

      const dataCrimes = {
        type: "FeatureCollection",
        features: props.dataCrimes.features.filter((feature: DataFeature) => {
          if (!props.showOlder && feature.properties.dateMatch === "older_context")
            return false;
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
          if (!props.showOlder && feature.properties.dateMatch === "older_context")
            return false;
          if (typeof feature.properties.category !== "string")
            return activeCategoriesIds.includes(
              feature.properties.category?.id
            );
          return false;
        }),
      };

      const withOlderCounts = (features: DataFeature[]) => ({
        type: "FeatureCollection",
        features: [...features].sort((a, b) => {
          const aOlder = a.properties.dateMatch === "older_context";
          const bOlder = b.properties.dateMatch === "older_context";
          return aOlder === bOlder ? 0 : aOlder ? -1 : 1;
        }).map((feature) => ({
          ...feature,
          properties: {
            ...feature.properties,
            olderCount: feature.properties.dateMatch === "older_context" ? 1 : 0,
          },
        })),
      });
      dataCrimesSource?.setData(withOlderCounts(dataCrimes.features));
      data311sSource?.setData(withOlderCounts(data311s.features));
      heatMapDataCrimesSource?.setData({
        type: "FeatureCollection",
        features: dataCrimes.features.filter((feature: DataFeature) => feature.properties.dateMatch !== "older_context"),
      });
      heatMapData311sSource?.setData({
        type: "FeatureCollection",
        features: data311s.features.filter((feature: DataFeature) => feature.properties.dateMatch !== "older_context"),
      });
    } catch (e) {
      Log.error({ msg: e, ...Log.data });
    }
  }, [props.activeCategories, props.dataCrimes, props.data311s, props.showOlder]);

  return <div id="map"></div>;
}
