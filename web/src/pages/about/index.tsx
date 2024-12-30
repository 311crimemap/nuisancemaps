import { useState, useEffect } from "react";
import Meta from "../../Meta";
import Attribution from "./_attribution";
import Description from "./_description";
import Terms from "./_terms";
import Privacy from "./_privacy";
import { getData } from "../../Util";
import { FeatureCollection } from "../../types/features";

export default function About() {
  const [sources, setSources] = useState<FeatureCollection>({
    type: "FeatureCollection",
    features: [],
  });
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    const initURL: string = `${import.meta.env.VITE_API_SERVER_URL}/init`;

    Promise.all([getData(initURL)]).then(([dataSourceCategories]) => {
      setSources(dataSourceCategories.data.sources);
      setIsLoaded(true);
    });
  }, []);

  if (!isLoaded) return;

  return (
    <>
      <Meta pageName="About" />
      <div className="container mx-auto mt-16">
        <div className="flex flex-col sm:flex-row py-1 justify-center">
          <div id="menu" className="sm:mt-8 pr-4">
            <ul className="fixed menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
              <li>
                <a href="#311CrimeMap">About</a>
              </li>
              <li>
                <a href="#terms">Terms of Service</a>
              </li>
              <li>
                <a href="#privacy">Privacy Policy</a>
              </li>
              <li>
                <a href="#attribution">Attribution</a>
              </li>
            </ul>
          </div>

          <div id="content" className="sm:ml-60 px-4 mt-6 sm:-mt-2">
            <Description />
            <div className="divider"></div>

            <Terms />
            <div className="divider"></div>

            <Privacy />
            <div className="divider"></div>

            <Attribution sources={sources} />
          </div>
        </div>
      </div>
    </>
  );
}
