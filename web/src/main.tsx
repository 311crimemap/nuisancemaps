import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import ErrorPage from "./error-page";
import Navbar from "./Navbar.tsx";
import App from "./App.tsx";
import Cities from "./Cities.tsx";
import About from "./About.tsx";
import "./index.css";

const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <>
        <Navbar />
        <App />
      </>
    ),
    errorElement: <ErrorPage />,
  },
  {
    path: "/cities",
    element: (
      <>
        <Navbar />
        <Cities />
      </>
    ),
  },
  {
    path: "/about",
    element: (
      <>
        <Navbar />
        <About />
      </>
    ),
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
