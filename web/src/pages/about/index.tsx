import { Link } from "react-router-dom";
import Meta from "../../Meta";
import Description from "./_description";

export default function About() {

  return (
    <>
      <Meta pageName="About" />
      <div className="container mx-auto mt-16">
        <div className="flex flex-col sm:flex-row py-1 justify-center">
          <div id="menu" className="sm:mt-8 pr-4">
            <ul className="fixed menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
              <li>
                <Link to="/about">About</Link>
              </li>
              <li>
                <Link to="/legal#terms">Terms of Service</Link>
              </li>
              <li>
                <Link to="/legal#privacy">Privacy Policy</Link>
              </li>
              <li>
                <Link to="/legal#attribution">Attribution</Link>
              </li>
            </ul>
          </div>

          <div id="content" className="sm:ml-60 px-4 mt-6 sm:-mt-2">
            <Description />
            <div className="divider"></div>
          </div>
        </div>
      </div>
    </>
  );
}
