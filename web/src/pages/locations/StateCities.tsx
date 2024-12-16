import { slugify } from "../../Util";
import { StateCityMap } from "./statecitymap";
import CityData from "./CityData";

interface StateCitiesProps {
  stateCityMap: StateCityMap;
}

export default function StateCities({ stateCityMap }: StateCitiesProps) {
  const buildState = () => {
    return Object.keys(stateCityMap)
      .sort()
      .map((state: string, i: number) => {
        const offset = i == 0 ? "-top-64" : "-top-28";
        return (
          <li>
            <div className="font-bold text-xl pt-6">
              <a
                id={`${slugify(state)}`}
                className={`block relative invisible ${offset}`}
              ></a>
              {state}
            </div>

            <ul className="list-none">
              {Object.values(stateCityMap[state])
                .sort()
                .map((cityData) => {
                  return <CityData cityData={cityData} />;
                })}
            </ul>
          </li>
        );
      });
  };

  return (
    <article className="prose">
      <ul className="list-none ml-[-1.625rem]">{buildState()}</ul>
    </article>
  );
}
