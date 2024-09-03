import { slugify } from "../../Util";

export default function StatesMenu({ states }) {
  return (
    <ul className="fixed menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
      <hr />
      {states.sort().map((state) => {
        return (
          <li key={state}>
            <a href={`#${slugify(state)}`}>{state}</a>
          </li>
        );
      })}
    </ul>
  );
}
