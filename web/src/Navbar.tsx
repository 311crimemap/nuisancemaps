export default function Navbar() {
  return (
    <nav data-theme="dracula" className="navbar bg-base-100">
      <div className="flex-1">
        <a className="btn btn-ghost text-xl">Nuisance Map</a>
      </div>
      <div className="flex-none">
        <ul className="menu menu-horizontal px-1">
          <li>
            <a>Cities</a>
          </li>
          <li>
            <a>About</a>
          </li>
        </ul>
      </div>
    </nav>
  );
}
