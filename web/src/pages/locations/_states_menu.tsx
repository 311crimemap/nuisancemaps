import { slugify } from "../../Util";

interface StatesMenuProps {
  states: string[];
}

export default function StatesMenu({ states }: StatesMenuProps) {
  // reduce to most populous states
  const mobileStatesMap: {[string:string]: boolean} = {
    California: true,
    Florida: true,
    Illinois: true,
    "New York": true,
    Pennsylvania: true,
    Texas: true,
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
