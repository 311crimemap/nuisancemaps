import { useState, useEffect } from "react";
import StatesMenu from "./_states_menu";
import StateCities from "./_state_cities";
import { getData } from "../../Util";
import { FeatureCollection } from "../../types/features";
import { StateCityMap } from "./statecitymap";

export default function Locations() {
  const [sources, setSources] = useState<FeatureCollection>({
    type: "FeatureCollection",
    features: [],
  });
  const [isLoaded, setIsLoaded] = useState(false);

  const buildStateCityMap = (sources: FeatureCollection): StateCityMap => {
    const stateCityMap: StateCityMap = {};

    for (const feature of sources.features) {
      const { name, city, state } = { ...feature.properties };
      stateCityMap[state] = stateCityMap[state] || [];
      stateCityMap[state].push({
        name,
        city,
        state,
      });
    }

    return stateCityMap;
  };

  useEffect(() => {
    const initURL = `${import.meta.env.VITE_API_SERVER_URL}/init`;

    Promise.all([getData(initURL)]).then(([dataSourceCategories]) => {
      setSources(dataSourceCategories.data.sources);
      setIsLoaded(true);
    });
  }, []);

  /*
   * RENDER
   */
  if (!isLoaded) return;

  const stateCityMap: StateCityMap = buildStateCityMap(sources);

  return (
    <div className="container mx-auto mt-16">
      <div className="flex flex-col sm:flex-row py-1 justify-center">
        <div id="menu" className="sm:mt-8 pr-4">
          <StatesMenu states={Object.keys(stateCityMap)} />
        </div>

        <div id="content" className="sm:ml-60 px-4 mt-6 sm:-mt-2">
          {/* spacer */}
          <article className="prose sm:w-[39ch] md:w-[53ch] lg:w-[65ch]">
            <a
              id="311CrimeMap"
              className="block relative invisible -top-64"
            ></a>
            <h2>Locations</h2>
            <div className="divider"></div>
          </article>

          <StateCities stateCityMap={stateCityMap} />
        </div>
      </div>
    </div>
  );
}
