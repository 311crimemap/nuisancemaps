import rbush from "rbush";
import * as turf from "@turf/turf";
import { MapGeoJSONFeature } from "maplibre-gl";

import {
  DataFeatureCollection,
  DataFeature,
  DataCategory,
  DataGeometry,
} from "../../types/datafeatures";

interface TreeItem {
  minX: number;
  minY: number;
  maxX: number;
  maxY: number;
  feature: MapGeoJSONFeature;
  index: number;
}
/*
 * overlapping points on different layers
 * will move only unclustered points (avoid moving clusters for now)
 */

export default class DuplicatePointNudge {
  readonly triggerDistance = 0.00005;
  readonly offsetDistance = 0.000175;

  private features: MapGeoJSONFeature[];
  private tree: any;
  private items: TreeItem[];
  private overlaps: MapGeoJSONFeature[];
  private featMap: { [key: string]: MapGeoJSONFeature };

  constructor(features: MapGeoJSONFeature[]) {
    this.features = features;
    this.tree = new rbush();
    this.items = [];
    this.overlaps = [];
    this.featMap = {};
  }

  init() {
    this.loadTree();
    this.overlaps = this.checkOverlap();
    this.featMap = this.buildMap();
  }

  nudge(data: DataFeatureCollection, dataType: String) {
    if (Object.keys(this.featMap).length == 0) return;

    for (let i = 0; i < data.features.length; i++) {
      const feature: DataFeature = data.features[i];
      const overlapFeature = this.featMap[feature.properties.reportNum];

      if (typeof feature.properties.category == "string") {
        feature.properties.category = JSON.parse(
          feature.properties.category
        ) as DataCategory;
      }

      if (overlapFeature && feature.properties.category.dataType == dataType) {
        //replace
        data.features[i] = {
          ...data.features[i],
          geometry: overlapFeature.geometry as DataGeometry,
        };
      }
    }
  }

  private buildMap() {
    this.featMap = {};
    for (const feature of this.overlaps) {
      this.featMap[feature.properties.reportNum] = feature;
    }
    return this.featMap;
  }

  // Function to offset a point by a small distance
  private offsetPoint(feature: MapGeoJSONFeature, distance: number) {
    const angle = 2 * Math.PI;
    const dx = distance * Math.cos(angle);
    const dy = distance * Math.sin(angle);
    const geometry = feature.geometry as GeoJSON.Point;
    return turf.point([
      geometry.coordinates[0] + dx,
      geometry.coordinates[1] + dy,
    ]);
  }

  private checkOverlap(): MapGeoJSONFeature[] {
    // Check for overlapping points and offset them
    const offsetFeatures: MapGeoJSONFeature[] = [];

    this.items.forEach((item: TreeItem) => {
      const { feature } = item; //MapGeoJSONFeature
      const overlaps = this.tree.search(item);

      if (overlaps.length > 1) {
        //NB: offsetFeature only contains updated geometry (not properties)
        const offsetFeature = this.offsetPoint(feature, this.offsetDistance);
        const geometry = feature.geometry as GeoJSON.Point;
        geometry.coordinates = offsetFeature.geometry.coordinates;

        //collect
        offsetFeatures.push(feature);
      }
    });

    return offsetFeatures.filter((feature) => feature.id === undefined);
  }

  private loadTree() {

    this.items = this.features.map((feature, index) => {
      const geometry = feature.geometry as GeoJSON.Point;

      return {
        minX: geometry.coordinates[0] - this.triggerDistance,
        minY: geometry.coordinates[1] - this.triggerDistance,
        maxX: geometry.coordinates[0] + this.triggerDistance,
        maxY: geometry.coordinates[1] + this.triggerDistance,
        feature,
        index,
      } as TreeItem;
    });

    this.tree.load(this.items);
  }
}
