import { Link } from "react-router-dom";
import { slugify } from "../../Util";

export default function StateCities({ states }) {

    return (


        Object.keys(states).sort().map((state) => {
            return (
                <article key={state} className="prose">
                    <h4>{state}</h4>
                    <ul>
                        {
                            Object.values(states[state]).sort().map(val => {
                                return (
                                    <li key={val.city}>
                                        <Link to={`/${slugify(val.city)}`} >{val.city} </Link>
                                    </li>
                                )

                            })
                        }

                    </ul>
                    <div className="divider"></div>
                </article>

            )
        })


    )
}
