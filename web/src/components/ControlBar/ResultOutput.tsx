export default function ResultOutput({ map, DATASOURCES }) {
  if (!map) return;
  if (!DATASOURCES) return;

  const dataCrimesSource = map.getSource(DATASOURCES.DataCrimes);
  const data311sSource = map.getSource(DATASOURCES.Data311s);

  return (
    <div className="flex flex-col justify-center items-center">
      <div className="ml-5">
        <p>Crime: {dataCrimesSource?._data?.features?.length || 0}</p>
        <p>311: {data311sSource?._data?.features?.length || 0}</p>
      </div>
    </div>
  );
}
