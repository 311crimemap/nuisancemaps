
import React from "react";
import ReactDOM from "react-dom/client";
import { HelmetProvider } from "react-helmet-async";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import ErrorPage from "./error-page";
import Navbar from "./Navbar.tsx";
import App from "./App.tsx";
import Locations from "./pages/locations/index.tsx";
import About from "./pages/about/index.tsx";
import Legal from "./pages/legal/index.tsx";
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
    path: "/:city",
    element: (
      <>
        <Navbar />
        <App />
      </>
    ),
    errorElement: <ErrorPage />,
  },

  {
    path: "/locations",
    element: (
      <>
        <Navbar />
        <Locations />
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
  {
    path: "/legal",
    element: (
      <>
        <Navbar />
        <Legal />
      </>
    ),
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <HelmetProvider>
          <RouterProvider router={router} />
        </HelmetProvider>
    </React.StrictMode>
);
