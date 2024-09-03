import { Link } from "react-router-dom";
import { slugify } from "../../Util";

export default function StateCities({ stateCityMap }) {
  return Object.keys(stateCityMap)
    .sort()
    .map((state, i) => {
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
