import { GeocodingControl } from "@maptiler/geocoding-control/react";

/*
 * https://docs.maptiler.com/sdk-js/modules/geocoding/api/api-reference/#geocoding-options
 *
 * https://github.com/maptiler/maptiler-geocoding-control/blob/main/src/GeocodingControl.svelte
 * https://github.com/maptiler/maptiler-geocoding-control/blob/main/src/maplibregl-controller.ts#L30'
 *   set params above in useMap call of createMapLibreGlMapController(...params)
 *
 */
export function Search({ mapController }) {
  const API_KEY = import.meta.env.VITE_MAPTILER_API_KEY;
  return (
    <div className="map-wrap">
      <div className="geocoding">
        <GeocodingControl
          apiKey={API_KEY}
          mapController={mapController}
          country="us"
          showFullGeometry={false}
          flyTo={{ speed: 30, duration: 2 }}
          markerOnSelected={false}
        />
      </div>
    </div>
  );
}
