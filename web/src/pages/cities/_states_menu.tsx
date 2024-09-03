import { slugify } from "../../Util";

export default function StatesMenu({ states }) {
  // reduce to most populous states
  const mobileStatesMap = {
    "California": true,
    "Florida": true,
    "Illinois": true,
    "New York": true,
    "Pennsylvania": true,
    "Texas": true,
  };



  return (
    <ul className="fixed menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
      <hr />
      {states.sort().map((state) => {
        const hideMobile = !mobileStatesMap[state];

        return (
          <li key={state} className={hideMobile ? "hidden sm:block" : ""}>
            <a href={`#${slugify(state)}`}>{state}</a>
          </li>
        );
      })}
    </ul>
  );
}
