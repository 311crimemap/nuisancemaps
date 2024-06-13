/*
 * TODO:
 * toggle visible and pass cluster elements when cluster is clicked
 * make sure to hook into close event
 */

import { useState, useEffect } from "react";
import FeatureView from "./FeatureView";

export default function FeatureListComponent({
  map,
  activeFeatureList,
  featureZoomLevel,
}) {
  const LIST_VIEW_COUNT = 3;

  const [isVisible, setIsVisible] = useState(false);

  const { source, features } = activeFeatureList;

  useEffect(() => {
    //1. unclustered feature  at any zoom -> show
    //2. random click, or cluster at high zoom (large clusters)
    //    don't toggle, allow default behavior to zoom in
    //3. else it's a cluster at acceptable zoom -> show

    if (features && features.length == 1) {
      setIsVisible(true);
    } else if (!features || map.getZoom() < featureZoomLevel) {
      setIsVisible(false);
    } else {
      setIsVisible(true);
    }
  }, [features]);

  const sorted_features = (features || []).sort(
    (a, b) =>
      new Date(b.properties.reportedAt) - new Date(a.properties.reportedAt)
  );

  const featuresLen = (features || []).length;

  const style = {
    display: isVisible ? "block" : "none",
  };

  return (

    <div id="feature-list-component" className="shadow-md" style={style}>
      <ul>
        {(sorted_features || []).map((feature, i) => {
          return (
            <FeatureView
              key={`view-${feature.properties.reportNum}`}
              feature={feature}
              featuresLen={featuresLen}
              isLast={i+1 == features.length}
              initListMode={featuresLen > LIST_VIEW_COUNT}
            />
          );
        })}
      </ul>
    </div>

  );
}
