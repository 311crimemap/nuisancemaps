import { FeatureCollection } from "../../types/features";
import { Map } from "maplibre-gl";

interface MapInfoProps {
  map: Map;
  dataCrimes: FeatureCollection;
  data311s: FeatureCollection;
  isDataLoading: boolean;
}

export default function MapInfo({
  map,
  dataCrimes,
  data311s,
  isDataLoading,
}: MapInfoProps) {
  if (!map) return;

  const MAX_DATA_RECORDS: number = parseInt(
    import.meta.env.VITE_MAX_DATA_RECORDS
  );

  const isMax: boolean =
    (dataCrimes?.features?.length || 0) >= MAX_DATA_RECORDS ||
    (data311s?.features?.length || 0) >= MAX_DATA_RECORDS;

  const numCrime: string | number = isDataLoading
    ? "-"
    : dataCrimes.isDefaultData
    ? 0
    : dataCrimes?.features?.length;
  const num311: string | number = isDataLoading
    ? "-"
    : data311s.isDefaultData
    ? 0
    : data311s?.features?.length;

  const dataTip: string = `Warning: maximum result count set to ${MAX_DATA_RECORDS.toLocaleString()}. Zoom in, and/or adjust dates to shorten time frame and reduce number of results.`;

  return (
    <div className="flex flex-row justify-center">
      {isMax && (
        <div className="tooltip tooltip-left mr-3" data-tip={dataTip}>
          <div className="indicator">
            <span className="indicator-item indicator-middle indicator-center badge badge-xs badge-warning"></span>
          </div>
        </div>
      )}

      <div
        className="flex flex-col justify-center items-center w-16 ml-5"
        title="number of crime records"
      >
        <div>{numCrime.toLocaleString()}</div>
        <span className="text-xs">crime</span>
      </div>
      <div
        className="flex flex-col justify-center items-center w-16 ml-5"
        title="number of 311 records"
      >
        <div>{num311.toLocaleString()}</div>
        <span className="text-xs">311</span>
      </div>
    </div>
  );
}
