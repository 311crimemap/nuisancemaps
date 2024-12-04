import { Link } from "react-router-dom";

export default function Navbar() {
  return (
    <nav data-theme="dracula" className="navbar bg-base-100 fixed">
      <div className="flex-1">
        <Link to="/" className="btn btn-ghost text-xl" id="homelink">
          311 Crime Map
        </Link>
      </div>
      <div className="flex-none">
        <ul className="menu menu-horizontal px-1">
          <li>
            <Link to="/cities">Cities</Link>
          </li>
          <li>
            <Link to="/about">About</Link>
          </li>
        </ul>
      </div>
    </nav>
  );
}
