export default function Attribution({ sources }) {
  if (!sources || sources.length == 0) return;

  sources.features.sort((a, b) => a.properties.city > b.properties.city);

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
          {sources.features.map((feature) => {
            const { city, attribution } = { ...feature.properties };
            const markup = { __html: attribution };

            return (
              <tr>
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
