import { DataFeature, DataFeatureCollection } from "../../types/datafeatures";
import { Category } from "../../types/category";
import { Map } from "maplibre-gl";
import { DATASTATUS } from "../../types/datastatus";

interface MapInfoProps {
  map: Map;
  dataCrimes: DataFeatureCollection;
  data311s: DataFeatureCollection;
  activeCategories: Category[];
  dataStatus: DATASTATUS;
  showOlder: boolean;
  setShowOlder: (value: boolean) => void;
}

export default function MapInfo({
  map,
  dataCrimes,
  data311s,
  activeCategories,
  dataStatus,
  showOlder,
  setShowOlder,
}: MapInfoProps) {
  const MAX_DATA_RECORDS: number = parseInt(
    import.meta.env.VITE_MAX_DATA_RECORDS
  );

  const isMax: boolean =
    (dataCrimes?.features?.length || 0) >= MAX_DATA_RECORDS ||
    (data311s?.features?.length || 0) >= MAX_DATA_RECORDS;

  const numCrime: string | number =
    dataStatus !== DATASTATUS.OK
      ? "-"
      : dataCrimes.isDefaultData
      ? 0
      : dataCrimes?.features?.length;
  const num311: string | number =
    dataStatus !== DATASTATUS.OK
      ? "-"
      : data311s.isDefaultData
      ? 0
      : data311s?.features?.length;

  const enabled = new Set(activeCategories.filter((category) => category.checked).map((category) => category.id));
  const bounds = map.getBounds();
  const visible = (data: DataFeatureCollection) => data.isDefaultData ? [] : data.features.filter((feature: DataFeature) =>
    typeof feature.properties.category !== "string" &&
    enabled.has(feature.properties.category.id) &&
    bounds.contains([feature.geometry.coordinates[0], feature.geometry.coordinates[1]])
  );
  const visibleCrime = visible(dataCrimes);
  const visible311 = visible(data311s);
  const olderCrime = visibleCrime.filter((feature) => feature.properties.dateMatch === "older_context");
  const older311 = visible311.filter((feature) => feature.properties.dateMatch === "older_context");
  const olderReports = [...olderCrime, ...older311];
  const latestOlderDate = olderReports
    .map((feature) => feature.properties.reportedAt?.slice(0, 10))
    .filter((date): date is string => Boolean(date))
    .sort()
    .pop();
  const reportType = olderCrime.length && older311.length
    ? "reports"
    : olderCrime.length ? "crime reports" : "311 reports";
  const [year, month, day] = (latestOlderDate ?? "").split("-");
  const formattedDate = latestOlderDate ? `${month}/${day}/${year}` : null;
  const message = formattedDate
    ? `Reports unreleased for date range. ${showOlder ? "Including most recent" : "Most recent"} ${reportType} ${showOlder ? "from" : "available from"} ${formattedDate}.`
    : "";
  return (
    <div className="flex items-center justify-end w-full min-w-0 gap-4 text-sm" aria-live="polite">
      <span className="hidden min-[960px]:block flex-1 min-w-0 text-center text-xs leading-tight" title={message}>
        {dataStatus === DATASTATUS.OK ? message : ""}
      </span>
      <div className="flex shrink-0 flex-col justify-center whitespace-nowrap leading-tight">
        <label className="flex items-center justify-center cursor-pointer">
          <input type="checkbox" className="cursor-pointer" checked={!showOlder}
            onChange={(event) => setShowOlder(!event.target.checked)} />
          <span className="label-text pl-2">Hide older reports</span>
        </label>
        <span className="hidden min-[960px]:block text-center text-xs">Faded markers show older data</span>
      </div>
      <div className="hidden 2xl:flex shrink-0 items-center">
        <div className="flex flex-col justify-center items-center w-16 ml-3" title="crime records returned, including older reports">
          <div>{numCrime.toLocaleString()}</div>
          <span className="text-xs">crime</span>
        </div>
        <div className="flex flex-col justify-center items-center w-16 ml-3" title="311 records returned, including older reports">
          <div>{num311.toLocaleString()}</div>
          <span className="text-xs">311</span>
        </div>
        {isMax && <span className="badge badge-xs badge-warning" title="Results are capped. Zoom in or shorten the date range to see more." />}
      </div>
    </div>
  );
}
