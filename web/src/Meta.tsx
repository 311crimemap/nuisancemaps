import { Helmet, HelmetData } from "react-helmet-async";
import { useState, useEffect } from "react";
interface MetaProps {
  pageName: string;
  path: string;
}

export default function Meta({ pageName, path }: MetaProps) {
  const title = pageName ? `311 Crime Map - ${pageName}` : "311 Crime Map";
  const canonicalPath = path || "";

  return (
    <Helmet key={title}>
      <title>{title}</title>
      <link rel="canonical" href={`https://311crimemap.com/${canonicalPath}`} />
      <meta property="og:title" content={title} />
      <meta property="twitter:title" content={title} />
    </Helmet>
  );
}
