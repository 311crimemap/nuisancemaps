import { MapController } from "@maptiler/geocoding-control/types";
import { GeocodingControl } from "@maptiler/geocoding-control/react";

/*
 * https://docs.maptiler.com/sdk-js/modules/geocoding/api/api-reference/#geocoding-options
 *
 * https://github.com/maptiler/maptiler-geocoding-control/blob/main/src/GeocodingControl.svelte
 * https://github.com/maptiler/maptiler-geocoding-control/blob/main/src/maplibregl-controller.ts#L30'
 *   set params above in useMap call of createMapLibreGlMapController(...params)
 *
 */
interface SearchProps {
    mapController: MapController
}

export function Search({ mapController } : SearchProps) {
  const API_KEY = import.meta.env.VITE_MAPTILER_API_KEY;
  return (
    <div className="map-wrap">
      <div className="geocoding">
        <GeocodingControl
          apiKey={API_KEY}
          mapController={mapController}
          country="us"
          showFullGeometry={false}
          markerOnSelected={false}
        />
      </div>
    </div>
  );
}
