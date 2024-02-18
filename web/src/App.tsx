import { useState, useEffect } from 'react'
import './App.css'

import MapComponent from "./MapComponent";

function App() {

    const dataCrimesURL = "http://localhost:8080/datacrimes?limit=500";
    const data311sURL = "http://localhost:8080/data311s?limit=500";

    const [dataCrimes, setDataCrimes] = useState([]);
    const [data311s, setData311s] = useState([]);

    const get_data = (url: string) =>  {
        return fetch(url).then( res => res.json());
    };


    useEffect(() => {

        Promise.all([get_data(dataCrimesURL), get_data(data311sURL)])
            .then(res => {
                setDataCrimes(res[0]);
                setData311s(res[1]);
            });
    }, []);


    return (
        <>
            <MapComponent dataCrimes={dataCrimes} data311s={data311s} />
        </>
    )
}

export default App
