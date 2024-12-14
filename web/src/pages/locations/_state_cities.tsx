import { Link } from "react-router-dom";
import { slugify } from "../../Util";
import { StateCityMap } from "./statecitymap";

interface StateCitiesProps {
  stateCityMap: StateCityMap;
}

export default function StateCities({ stateCityMap }: StateCitiesProps) {
  return Object.keys(stateCityMap)
    .sort()
    .map((state: string, i: number) => {
      const offset = i == 0 ? "-top-64" : "-top-28";

      return (
        <article key={state} className="prose">
          <a
            id={`${slugify(state)}`}
            className={`block relative invisible ${offset}`}
          ></a>
          <h4>{state}</h4>
          <ul>
            {Object.values(stateCityMap[state])
              .sort()
              .map((val) => {
                return (
                  <li key={val.city}>
                    <Link to={`/${slugify(val.city)}`}>{val.city} </Link>
                  </li>
                );
              })}
          </ul>
          <div className="divider"></div>
        </article>
      );
    });
}
