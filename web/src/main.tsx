import React from 'react'
import ReactDOM from 'react-dom/client'
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import ErrorPage from "./error-page";
import Navbar from "./Navbar.tsx";
import App from './App.tsx'
import './index.css'

const router = createBrowserRouter([
    {
        path: "/",
        element: <App />,
        errorElement: <ErrorPage />
    },
    {
        path: "/test",
        element: <div>Hello world!</div>,
    }
]);


ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <Navbar />
        <RouterProvider router={router} />
    </React.StrictMode>,
)
