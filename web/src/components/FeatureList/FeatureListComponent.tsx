/*
 * TODO:
 * toggle visible and pass cluster elements when cluster is clicked
 * make sure to hook into close event
 */

import { useState, useEffect } from "react";
import FeatureView from "./FeatureView";
import ActiveFeatures from "../../types/activefeatures.ts";
import { DataFeature } from "../../types/datafeatures.ts";

interface FeatureListComponentProps {
  activeFeatures: ActiveFeatures;
}

export default function FeatureListComponent({
  activeFeatures,
}: FeatureListComponentProps) {
  const LIST_VIEW_COUNT = 3;

  const [isVisible, setIsVisible] = useState(false);

  const { features, clusterExpansionZoom, clusterMaxZoom } = activeFeatures;

  useEffect(() => {
    //no features, or we can continue to zoom and break upt he cluster
    //in this case, don't show the featureList
    if (!features || clusterExpansionZoom < clusterMaxZoom) {
      setIsVisible(false);
    } else {
      //arrived at some terminal cluster or individual point, show the panel
      setIsVisible(true);
    }
  }, [features]);

  const sorted_features = (features || []).sort(
    (a: DataFeature, b: DataFeature) =>
      new Date(b.properties.reportedAt).getTime() -
      new Date(a.properties.reportedAt).getTime()
  );

  const featuresLen = (features || []).length;

  const style = {
    display: isVisible ? "block" : "none",
  };

  return (
    <div id="feature-list-component" className="shadow-md" style={style}>
      <ul>
        {(sorted_features || []).map((feature: DataFeature, i: number) => {
          return (
            <FeatureView
              key={`view-${feature.properties.reportNum}-${i}`}
              feature={feature}
              featuresLen={featuresLen}
              isLast={i + 1 == features.length}
              initListMode={featuresLen > LIST_VIEW_COUNT}
            />
          );
        })}
      </ul>
    </div>
  );
}
