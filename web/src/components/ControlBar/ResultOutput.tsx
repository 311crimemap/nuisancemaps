export default function ResultOutput({ map, dataCrimes, data311s }) {
  if (!map) return;

  return (
    <div className="flex flex-col justify-center items-center">
      <div className="ml-5">
        <p>Crime: {dataCrimes?.features?.length || 0}</p>
        <p>311: {data311s?.features?.length || 0}</p>
        <p>Zoom: {map.getZoom()}</p>
      </div>
    </div>
  );
}
