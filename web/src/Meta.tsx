import { Helmet } from "react-helmet-async";
interface MetaProps {
  pageName: string;
  path: string | undefined;
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
