
export default function States({states}) {

    return (
        <ul className="fixed menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
            <li>
                <a href="#311CrimeMap">
                    States
                </a>
            </li>

            <hr/>
            {states.map(state => {
                return (
                <li key={state}>
                    <a href={`#${state}`}>{state}</a>
                </li>
                )

            })}

        </ul>
    );
}
