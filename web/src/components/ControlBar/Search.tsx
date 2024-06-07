import { GeocodingControl } from "@maptiler/geocoding-control/react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons";

/*
 * https://docs.maptiler.com/sdk-js/modules/geocoding/api/api-reference/#geocoding-options
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
          flyTo={{ speed: 30, duration: 2 }}
        />
      </div>
    </div>
  );
}
