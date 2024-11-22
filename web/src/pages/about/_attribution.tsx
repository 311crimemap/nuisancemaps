import { FeatureCollection, Feature } from "../../types/features";

interface AttributionProps {
  sources: FeatureCollection;
}

export default function Attribution({ sources }: AttributionProps) {
  if (!sources || sources?.features?.length == 0) return;

  sources.features.sort((a:Feature, b:Feature) => {
    if (a.properties.city > b.properties.city) return 1;
    if (a.properties.city < b.properties.city) return -1;
    return 0;
  });

  return (
    <article className="prose">
      <a id="attribution" className="block relative invisible -top-28"></a>
      <h2> Data Attribution </h2>

      <table className="table">
        <thead>
          <tr>
            <th>City</th>
            <th>Attribution</th>
          </tr>
        </thead>

        <tbody>
          {sources.features.map((feature, i) => {
            const { city, attribution } = { ...feature.properties };
            const markup = { __html: attribution };

            return (
              <tr key={`${city}-${i}`}>
                <td>{city}</td>
                <td dangerouslySetInnerHTML={markup} />
              </tr>
            );
          })}
        </tbody>
      </table>
    </article>
  );
}
