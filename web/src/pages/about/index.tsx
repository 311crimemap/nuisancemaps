import Description from "./_description";
import Terms from "./_terms";
import Privacy from "./_privacy";

export default function About() {
  return (
    <div className="container mx-auto mt-16">
      <div className="flex flex-col sm:flex-row py-1">
        <div id="menu" className="sm:fixed sm:mt-8 w-full">
          <ul className="menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
            <li>
              <a href="#311CrimeMap">311 Crime Map</a>
            </li>
            <li>
              <a href="#terms">Terms and Conditions</a>
            </li>
            <li>
              <a href="#privacy">Privacy Policy</a>
            </li>
          </ul>
        </div>

        <div id="content" className="sm:ml-60 sm:mt-6 w-full p-4">

          <Description />
          <hr />

          <Terms />
          <hr />

          <Privacy />
        </div>
      </div>
    </div>
  );
}
