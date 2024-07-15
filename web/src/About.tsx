export default function About() {
    return <div className="container mx-auto mt-16">

        <div className="flex flex-col sm:flex-row py-1">

            <div id="menu" className="sm:fixed sm:mt-8 w-full">
                <ul className="menu menu-xs menu-horizontal sm:menu-sm sm:menu-vertical bg-base-200 w-full sm:w-56">
                    <li><a href="#311CrimeMap">311 Crime Map</a></li>
                    <li><a href="#terms">Terms and Conditions</a></li>
                    <li><a href="#privacy">Privacy Policy</a></li>
                </ul>
            </div>

            <div id="content" className="sm:ml-60 sm:mt-6 w-full p-4">

                {/* TODO: responsive anchor offset, wait contenet */}
                <a id="311CrimeMap" className="block relative invisible -top-64"></a>
                <h2>311 Crime Map</h2>

                <p>
                    This is a project
                </p>


                <p>
                    Contact
                </p>


                <hr />

                <h2 id="terms"> Terms And Conditions </h2>

                <hr />

                <h2 id="privacy"> Privacy Policy </h2>

            </div>
        </div>

    </div>;
}
