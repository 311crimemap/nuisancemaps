import rbush from "rbush";
import * as turf from "@turf/turf";

/*
 * overlapping points on different layers
 * will move only unclustered points (avoid moving clusters for now)
 */

export default class DuplicatePointNudge {
  readonly triggerDistance = 0.00005;
  readonly offsetDistance = 0.000175;

  constructor(features) {
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

  nudge(data: [any], dataType: String) {
    for (let i = 0; i < data.features.length; i++) {
      const feature = data.features[i];
      const overlapFeature = this.featMap[feature.properties.reportNum];

      if (overlapFeature && feature.properties.category.dataType == dataType) {
        //replace
        data.features[i] = {
          ...data.features[i],
          geometry: overlapFeature.geometry,
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
  private offsetPoint(point, distance) {
    //console.log("OFFSET detected", point, distance)
    //const angle = Math.random() * 2 * Math.PI;
    const angle = 2 * Math.PI;
    const dx = distance * Math.cos(angle);
    const dy = distance * Math.sin(angle);
    return turf.point([
      point.geometry.coordinates[0] + dx,
      point.geometry.coordinates[1] + dy,
    ]);
  }

  private checkOverlap() {
    // Check for overlapping points and offset them
    const offsetFeatures = [];
    this.items.forEach((item) => {
      const { feature } = item;
      const overlaps = this.tree.search(item);

      if (overlaps.length > 1) {
        //NB: offsetFeature only contains updated geometry (not properties)
        const offsetFeature = this.offsetPoint(feature, this.offsetDistance);
        feature.geometry.coordinates = offsetFeature.geometry.coordinates;

        //collect
        offsetFeatures.push(feature);
      }
    });

    return offsetFeatures.filter((feature) => feature.id === undefined);
  }

  private loadTree() {
    this.items = this.features.map((feature, index) => ({
      minX: feature.geometry.coordinates[0] - this.triggerDistance,
      minY: feature.geometry.coordinates[1] - this.triggerDistance,
      maxX: feature.geometry.coordinates[0] + this.triggerDistance,
      maxY: feature.geometry.coordinates[1] + this.triggerDistance,
      feature,
      index,
    }));

    this.tree.load(this.items);
  }
}
