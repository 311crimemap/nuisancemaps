import { useState, useEffect } from "react";
import States from "./_states";
import StateCities from "./_state_cities";

export default function Cities() {
  const [sources, setSources] = useState([]);
  const [isLoaded, setIsLoaded] = useState(false);

  const getData = async (url: string) => {
    return fetch(url).then((res) => res.json());
  };

  const calcStates = (sources) => {
    const states = {};
    for (const feature of sources.features) {
      const properties = feature.properties;
      const { name, city, state } = { ...properties };

      states[state] = states[state] || [];
      states[state].push({
        name,
        city,
        state,
      });
    }
    return states;
  };

  useEffect(() => {
    const initURL = `http://localhost:8080/init`;

    console.log("FETCH INIT", initURL);

    Promise.all([getData(initURL)]).then(([dataSourceCategories]) => {
      setSources(dataSourceCategories.data.sources);
      setIsLoaded(true);
    });
  }, []);

  /*
   * RENDER
   */
  if (!isLoaded) return;

  const states = calcStates(sources);

  return (
    <div className="container mx-auto mt-16">
      <div className="flex flex-col sm:flex-row py-1 justify-center">
        <div id="menu" className="sm:mt-8 pr-4">
          <States states={Object.keys(states)} />
        </div>

        <div id="content" className="sm:ml-60 px-4 mt-6 sm:-mt-2">
          <article className="prose w-[65ch]">
            <a
              id="311CrimeMap"
              className="block relative invisible -top-64"
            ></a>
            <h2>States</h2>
          </article>
          <div className="divider"></div>

          <StateCities states={states} />
        </div>
      </div>
    </div>
  );
}
